package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.AvatarImage
import com.example.ui.theme.AfricanGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.MintAccent
import com.example.webrtc.ActiveCallInfo
import com.example.webrtc.CallState

@Composable
fun CallActiveScreen(
    callInfo: ActiveCallInfo,
    onMuteToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F1E1B),
                        Color(0xFF07221C),
                        Color(0xFF04120F)
                    )
                )
            )
            .testTag("active_call_screen")
    ) {
        // Main calling content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Peer info & E2EE badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MintAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Appel chiffré de bout en bout (DTLS-SRTP)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    callInfo.peerName,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                val statusText = when (callInfo.callState) {
                    CallState.DIALING -> "Appel en cours..."
                    CallState.RINGING -> "Sonnerie..."
                    CallState.CONNECTED -> {
                        val min = callInfo.elapsedSeconds / 60
                        val sec = callInfo.elapsedSeconds % 60
                        "%02d:%02d".format(min, sec)
                    }
                    CallState.ENDED -> "Appel terminé"
                    else -> ""
                }

                Text(
                    statusText,
                    color = if (callInfo.callState == CallState.CONNECTED) MintAccent else Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    callInfo.signalingLog,
                    color = AfricanGold,
                    fontSize = 11.sp
                )
            }

            // Middle Section: Video feed or Pulsing Avatar with soundwave
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                if (callInfo.isVideo && callInfo.isVideoEnabled && callInfo.callState == CallState.CONNECTED) {
                    // Video simulated stream
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=600&q=80")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Flux vidéo distant",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .border(2.dp, MintAccent, RoundedCornerShape(24.dp))
                    )
                } else {
                    // Pulsing animated ring for audio call
                    Box(
                        modifier = Modifier
                            .size(160.dp * pulseScale)
                            .clip(CircleShape)
                            .background(MintAccent.copy(alpha = 0.15f))
                    )
                    AvatarImage(
                        url = callInfo.peerAvatarUrl,
                        seed = callInfo.peerPhoneNumber,
                        size = 120
                    )
                }
            }

            // In-call audio activity visualizer
            if (callInfo.callState == CallState.CONNECTED) {
                AudioWaveformVisualizer(
                    isPlaying = !callInfo.isMicMuted,
                    barsCount = 28,
                    color = MintAccent,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Bottom Section: Call controls
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speakerphone
                    IconButton(
                        onClick = onSpeakerToggle,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (callInfo.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            if (callInfo.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Haut-parleur",
                            tint = if (callInfo.isSpeakerOn) Color.Black else Color.White
                        )
                    }

                    // Video toggle
                    if (callInfo.isVideo) {
                        IconButton(
                            onClick = onVideoToggle,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (callInfo.isVideoEnabled) Color.White else Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                if (callInfo.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Caméra",
                                tint = if (callInfo.isVideoEnabled) Color.Black else Color.White
                            )
                        }

                        // Switch camera front/back
                        IconButton(
                            onClick = onSwitchCamera,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                Icons.Default.FlipCameraAndroid,
                                contentDescription = "Changer caméra",
                                tint = Color.White
                            )
                        }
                    }

                    // Mute Microphone
                    IconButton(
                        onClick = onMuteToggle,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (callInfo.isMicMuted) Color.Red else Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            if (callInfo.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White
                        )
                    }

                    // End Call (Red button)
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            Icons.Default.CallEnd,
                            contentDescription = "Terminer l'appel",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
