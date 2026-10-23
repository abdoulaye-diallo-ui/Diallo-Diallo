package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.repository.ChatRepository
import com.example.security.CryptoManager
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.AvatarImage
import com.example.ui.components.E2EEInspectionDialog
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    repository: ChatRepository,
    onBack: () -> Unit,
    onStartCall: (peerName: String, phone: String, avatar: String?, isVideo: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var conversation by remember { mutableStateOf<ConversationEntity?>(null) }
    val messages by repository.getMessages(conversationId).collectAsState(initial = emptyList())

    var inputText by remember { mutableStateOf("") }
    var selectedMessageForInspection by remember { mutableStateOf<MessageEntity?>(null) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    // Audio recorder simulation state
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }

    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        conversation = repository.getConversation(conversationId)
        repository.clearUnread(conversationId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Audio recording timer loop
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingDurationSec = 0
            while (isRecordingAudio) {
                delay(1000)
                recordingDurationSec++
            }
        }
    }

    Scaffold(
        modifier = Modifier.testTag("chat_screen_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showSafetyDialog = true }
                    ) {
                        conversation?.let { conv ->
                            AvatarImage(
                                url = conv.peerAvatarUrl,
                                seed = conv.peerPhoneNumber,
                                size = 38,
                                isOnline = conv.isOnline
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    conv.peerName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = AfricanGold,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        if (conv.isOnline) "En ligne • AES-256 GCM" else "Hors ligne • AES-256 GCM",
                                        fontSize = 11.sp,
                                        color = if (conv.isOnline) MintAccent else Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            conversation?.let {
                                onStartCall(it.peerName, it.peerPhoneNumber, it.peerAvatarUrl, true)
                            }
                        },
                        modifier = Modifier.testTag("video_call_button")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Appel vidéo WebRTC")
                    }
                    IconButton(
                        onClick = {
                            conversation?.let {
                                onStartCall(it.peerName, it.peerPhoneNumber, it.peerAvatarUrl, false)
                            }
                        },
                        modifier = Modifier.testTag("audio_call_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Appel audio WebRTC")
                    }
                    IconButton(onClick = { showSafetyDialog = true }) {
                        Icon(Icons.Default.Shield, contentDescription = "Vérifier la sécurité E2EE", tint = AfricanGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // E2EE Security reminder banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { showSafetyDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = AfricanGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Messages chiffrés de bout en bout. Touchez pour inspecter.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages, key = { it.messageId }) { msg ->
                    val isMe = msg.senderId == repository.currentUserId
                    MessageBubbleItem(
                        message = msg,
                        isMe = isMe,
                        onClick = { selectedMessageForInspection = msg }
                    )
                }
            }

            // Audio recording in progress bar
            AnimatedVisibility(visible = isRecordingAudio) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val min = recordingDurationSec / 60
                            val sec = recordingDurationSec % 60
                            Text(
                                "Enregistrement vocal... %02d:%02d".format(min, sec),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { isRecordingAudio = false }) {
                                Text("Annuler", color = Color.Red)
                            }
                            IconButton(
                                onClick = {
                                    val duration = recordingDurationSec.coerceAtLeast(1)
                                    isRecordingAudio = false
                                    coroutineScope.launch {
                                        repository.sendMessage(
                                            conversationId = conversationId,
                                            plainText = "Note vocale (${duration}s)",
                                            type = "AUDIO",
                                            audioDurationSec = duration
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer vocal", tint = MintAccent)
                            }
                        }
                    }
                }
            }

            // Bottom Input bar
            if (!isRecordingAudio) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(onClick = {
                                inputText += " 🇸🇳"
                            }) {
                                Icon(
                                    Icons.Default.Mood,
                                    contentDescription = "Emojis",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text("Message chiffré...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("message_input_field"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                maxLines = 4
                            )

                            IconButton(onClick = { showAttachmentSheet = !showAttachmentSheet }) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Joindre",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                // Instant photo preset from Dakar
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        conversationId = conversationId,
                                        plainText = "Photo envoyée",
                                        mediaUrl = "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?auto=format&fit=crop&w=800&q=80",
                                        type = "IMAGE",
                                        caption = "Monument de la Renaissance Africaine, Dakar 🇸🇳"
                                    )
                                }
                            }) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Caméra",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val textToSend = inputText.trim()
                                inputText = ""
                                coroutineScope.launch {
                                    repository.sendMessage(
                                        conversationId = conversationId,
                                        plainText = textToSend
                                    )
                                }
                            } else {
                                isRecordingAudio = true
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("send_button"),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                            contentDescription = if (inputText.isNotBlank()) "Envoyer" else "Enregistrer vocal"
                        )
                    }
                }
            }

            // Quick attachment drawer
            AnimatedVisibility(visible = showAttachmentSheet) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Partager un fichier chiffré", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttachmentOptionItem(
                                icon = Icons.Default.Image,
                                label = "Galerie",
                                color = Color(0xFF9C27B0),
                                onClick = {
                                    showAttachmentSheet = false
                                    coroutineScope.launch {
                                        repository.sendMessage(
                                            conversationId = conversationId,
                                            plainText = "Photo",
                                            mediaUrl = "https://images.unsplash.com/photo-1516026672322-bc52d61a55d5?auto=format&fit=crop&w=800&q=80",
                                            type = "IMAGE",
                                            caption = "Plage de Yoff au coucher du soleil"
                                        )
                                    }
                                }
                            )
                            AttachmentOptionItem(
                                icon = Icons.Default.Description,
                                label = "Document",
                                color = Color(0xFF2196F3),
                                onClick = {
                                    showAttachmentSheet = false
                                    coroutineScope.launch {
                                        repository.sendMessage(
                                            conversationId = conversationId,
                                            plainText = "Contrat E2EE.pdf",
                                            type = "DOC",
                                            fileName = "Protocole_Chiffrement_Samawakhtane.pdf"
                                        )
                                    }
                                }
                            )
                            AttachmentOptionItem(
                                icon = Icons.Default.Headphones,
                                label = "Audio",
                                color = Color(0xFFE91E63),
                                onClick = {
                                    showAttachmentSheet = false
                                    coroutineScope.launch {
                                        repository.sendMessage(
                                            conversationId = conversationId,
                                            plainText = "Audio vocal",
                                            type = "AUDIO",
                                            audioDurationSec = 24
                                        )
                                    }
                                }
                            )
                            AttachmentOptionItem(
                                icon = Icons.Default.LocationOn,
                                label = "Position",
                                color = MintAccent,
                                onClick = {
                                    showAttachmentSheet = false
                                    coroutineScope.launch {
                                        repository.sendMessage(
                                            conversationId = conversationId,
                                            plainText = "📍 Dakar-Plateau, Sénégal (14.6937° N, 17.4441° W)"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Inspection dialog for chosen message
    selectedMessageForInspection?.let { msg ->
        E2EEInspectionDialog(
            encryptedContent = msg.encryptedContent,
            onDismiss = { selectedMessageForInspection = null }
        )
    }

    // Safety number verification dialog
    if (showSafetyDialog && conversation != null) {
        SafetyNumberDialog(
            peerName = conversation!!.peerName,
            peerId = conversation!!.id,
            onDismiss = { showSafetyDialog = false }
        )
    }
}

@Composable
fun MessageBubbleItem(
    message: MessageEntity,
    isMe: Boolean,
    onClick: () -> Unit
) {
    var isAudioPlaying by remember { mutableStateOf(false) }
    val bubbleColor = if (isMe) {
        if (MaterialTheme.colorScheme.background == DarkBackground) ChatBubbleMeDark else ChatBubbleMeLight
    } else {
        if (MaterialTheme.colorScheme.background == DarkBackground) ChatBubblePeerDark else ChatBubblePeerLight
    }

    val decryptedText = remember(message.encryptedContent) {
        CryptoManager.decrypt(message.encryptedContent)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clickable { onClick() }
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                when (message.type) {
                    "IMAGE" -> {
                        message.mediaUrl?.let { url ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Image chiffrée",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        if (!message.caption.isNullOrBlank()) {
                            Text(
                                text = message.caption,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    "AUDIO" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { isAudioPlaying = !isAudioPlaying },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Lecture audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                AudioWaveformVisualizer(
                                    isPlaying = isAudioPlaying,
                                    barsCount = 20,
                                    color = if (isMe) MintAccent else EmeraldDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "0:%02d".format(message.audioDurationSec),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    "DOC" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    message.fileName ?: "Document chiffré.pdf",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("Document protégé (AES-256)", fontSize = 10.sp, color = Color.Gray)
                            }
                            Icon(Icons.Default.Download, contentDescription = "Télécharger", tint = Color.Gray)
                        }
                    }
                    else -> {
                        Text(
                            text = decryptedText,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(
                        text = format.format(Date(message.timestamp)),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            "READ" -> Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Lu",
                                tint = CheckmarkBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            "DELIVERED" -> Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Distribué",
                                tint = Color.Gray,
                                modifier = Modifier.size(15.dp)
                            )
                            else -> Icon(
                                Icons.Default.Done,
                                contentDescription = "Envoyé",
                                tint = Color.Gray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
