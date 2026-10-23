package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.repository.ChatRepository
import com.example.security.BiometricAuthenticator
import com.example.ui.screens.CallActiveScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LockScreenView
import com.example.ui.screens.MainHomeScreen
import com.example.ui.screens.QuickCameraScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SamawakhtaneTheme
import com.example.webrtc.WebRtcCallManager
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data class Chat(val conversationId: String) : Screen()
    data class UnlockConversation(val conversationId: String) : Screen()
    data object QuickCamera : Screen()
    data object Settings : Screen()
}

class MainActivity : FragmentActivity() {

    private lateinit var repository: ChatRepository
    private lateinit var callManager: WebRtcCallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ChatRepository.getInstance(applicationContext)
        callManager = WebRtcCallManager()

        lifecycleScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var isAppLocked by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

            val activeCallInfo by callManager.callState.collectAsState()

            val triggerBiometric: (onSuccess: () -> Unit) -> Unit = { onSuccess ->
                BiometricAuthenticator.authenticate(
                    activity = this@MainActivity,
                    title = "Verrouillage de Samawakhtane",
                    subtitle = "Veuillez authentifier votre identité pour continuer",
                    onSuccess = onSuccess,
                    onError = { error ->
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            SamawakhtaneTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        // Full app biometric lock
                        isAppLocked -> {
                            LockScreenView(
                                title = "Samawakhtane Verrouillé",
                                subtitle = "Veuillez vous authentifier par empreinte ou code PIN (1234)",
                                onBiometricClick = {
                                    triggerBiometric {
                                        isAppLocked = false
                                    }
                                },
                                onPinSuccess = {
                                    isAppLocked = false
                                },
                                onCancel = null
                            )
                        }

                        // WebRTC Active Call Overlay
                        activeCallInfo != null -> {
                            BackHandler {
                                callManager.endCall { duration ->
                                    activeCallInfo?.let { info ->
                                        lifecycleScope.launch {
                                            repository.logCall(
                                                peerName = info.peerName,
                                                peerPhoneNumber = info.peerPhoneNumber,
                                                peerAvatarUrl = info.peerAvatarUrl,
                                                isVideo = info.isVideo,
                                                isIncoming = false,
                                                isMissed = false,
                                                durationSec = duration
                                            )
                                        }
                                    }
                                }
                            }
                            CallActiveScreen(
                                callInfo = activeCallInfo!!,
                                onMuteToggle = { callManager.toggleMic() },
                                onVideoToggle = { callManager.toggleVideo() },
                                onSwitchCamera = { callManager.switchCamera() },
                                onSpeakerToggle = { callManager.toggleSpeaker() },
                                onEndCall = {
                                    callManager.endCall { duration ->
                                        activeCallInfo?.let { info ->
                                            lifecycleScope.launch {
                                                repository.logCall(
                                                    peerName = info.peerName,
                                                    peerPhoneNumber = info.peerPhoneNumber,
                                                    peerAvatarUrl = info.peerAvatarUrl,
                                                    isVideo = info.isVideo,
                                                    isIncoming = false,
                                                    isMissed = false,
                                                    durationSec = duration
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        // Specific locked chat PIN/Biometric gate
                        currentScreen is Screen.UnlockConversation -> {
                            val convoId = (currentScreen as Screen.UnlockConversation).conversationId
                            BackHandler { currentScreen = Screen.Home }
                            LockScreenView(
                                title = "Discussion Verrouillée",
                                subtitle = "Cette discussion contient des messages chiffrés protégés par biométrie (Code PIN: 1234)",
                                onBiometricClick = {
                                    triggerBiometric {
                                        currentScreen = Screen.Chat(convoId)
                                    }
                                },
                                onPinSuccess = {
                                    currentScreen = Screen.Chat(convoId)
                                },
                                onCancel = { currentScreen = Screen.Home }
                            )
                        }

                        // Inside a chat
                        currentScreen is Screen.Chat -> {
                            val convoId = (currentScreen as Screen.Chat).conversationId
                            BackHandler { currentScreen = Screen.Home }
                            ChatScreen(
                                conversationId = convoId,
                                repository = repository,
                                onBack = { currentScreen = Screen.Home },
                                onStartCall = { peerName, phone, avatar, isVideo ->
                                    callManager.initiateCall(peerName, phone, avatar, isVideo)
                                }
                            )
                        }

                        // Quick Camera Mode
                        currentScreen is Screen.QuickCamera -> {
                            val conversations by repository.getConversations().collectAsState(initial = emptyList())
                            BackHandler { currentScreen = Screen.Home }
                            QuickCameraScreen(
                                conversations = conversations,
                                onClose = { currentScreen = Screen.Home },
                                onPostToStory = { caption, imageUrl ->
                                    lifecycleScope.launch {
                                        repository.postStory(
                                            userName = "Moi",
                                            mediaUrl = imageUrl,
                                            caption = caption
                                        )
                                    }
                                },
                                onSendToChat = { convoId, caption, imageUrl ->
                                    lifecycleScope.launch {
                                        repository.sendMessage(
                                            conversationId = convoId,
                                            plainText = caption,
                                            mediaUrl = imageUrl,
                                            type = "IMAGE",
                                            caption = caption
                                        )
                                    }
                                }
                            )
                        }

                        // WhatsApp-style Full Settings
                        currentScreen is Screen.Settings -> {
                            BackHandler { currentScreen = Screen.Home }
                            SettingsScreen(
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { isDarkTheme = !isDarkTheme },
                                onBack = { currentScreen = Screen.Home }
                            )
                        }

                        // Main Home Tabs (Discussions, Statuts, Appels)
                        else -> {
                            MainHomeScreen(
                                repository = repository,
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { isDarkTheme = !isDarkTheme },
                                onLockApp = { isAppLocked = true },
                                onOpenChat = { convoId ->
                                    lifecycleScope.launch {
                                        val conv = repository.getConversation(convoId)
                                        if (conv?.isLocked == true) {
                                            currentScreen = Screen.UnlockConversation(convoId)
                                        } else {
                                            currentScreen = Screen.Chat(convoId)
                                        }
                                    }
                                },
                                onOpenStatusCamera = {
                                    currentScreen = Screen.QuickCamera
                                },
                                onOpenQuickCamera = {
                                    currentScreen = Screen.QuickCamera
                                },
                                onOpenSettings = {
                                    currentScreen = Screen.Settings
                                },
                                onStartCall = { peerName, phone, avatar, isVideo ->
                                    callManager.initiateCall(peerName, phone, avatar, isVideo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
