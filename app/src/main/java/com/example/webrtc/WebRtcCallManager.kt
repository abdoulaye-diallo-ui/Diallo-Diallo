package com.example.webrtc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CallState {
    IDLE,
    DIALING,
    RINGING,
    CONNECTED,
    ENDED
}

data class ActiveCallInfo(
    val peerName: String,
    val peerPhoneNumber: String,
    val peerAvatarUrl: String?,
    val isVideo: Boolean,
    val callState: CallState = CallState.DIALING,
    val isMicMuted: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isSpeakerOn: Boolean = false,
    val elapsedSeconds: Int = 0,
    val signalingLog: String = "Établissement du canal ICE / STUN..."
)

class WebRtcCallManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _callState = MutableStateFlow<ActiveCallInfo?>(null)
    val callState: StateFlow<ActiveCallInfo?> = _callState.asStateFlow()

    private var timerJob: Job? = null

    fun initiateCall(
        peerName: String,
        peerPhoneNumber: String,
        peerAvatarUrl: String?,
        isVideo: Boolean
    ) {
        timerJob?.cancel()
        _callState.value = ActiveCallInfo(
            peerName = peerName,
            peerPhoneNumber = peerPhoneNumber,
            peerAvatarUrl = peerAvatarUrl,
            isVideo = isVideo,
            callState = CallState.DIALING,
            isVideoEnabled = isVideo,
            isSpeakerOn = isVideo,
            signalingLog = "Initialisation SDP Offer (DTLS-SRTP)..."
        )

        scope.launch {
            delay(1200)
            _callState.value = _callState.value?.copy(
                callState = CallState.RINGING,
                signalingLog = "Sonnerie... Candidat ICE sélectionné"
            )

            delay(1800)
            _callState.value = _callState.value?.copy(
                callState = CallState.CONNECTED,
                signalingLog = "Canal P2P sécurisé actif (SRTP AES-256)"
            )

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000)
                _callState.value = _callState.value?.let { current ->
                    current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                }
            }
        }
    }

    fun toggleMic() {
        _callState.value = _callState.value?.let { it.copy(isMicMuted = !it.isMicMuted) }
    }

    fun toggleVideo() {
        _callState.value = _callState.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun switchCamera() {
        _callState.value = _callState.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun toggleSpeaker() {
        _callState.value = _callState.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun endCall(onEnded: (durationSec: Int) -> Unit = {}) {
        val duration = _callState.value?.elapsedSeconds ?: 0
        timerJob?.cancel()
        _callState.value = _callState.value?.copy(
            callState = CallState.ENDED,
            signalingLog = "Appel terminé"
        )
        scope.launch {
            delay(600)
            _callState.value = null
            onEnded(duration)
        }
    }
}
