package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationEntity
import com.example.data.repository.ChatRepository
import com.example.security.CryptoManager
import com.example.ui.components.AvatarImage
import com.example.ui.components.SafetyNumberDialog
import com.example.ui.theme.AfricanGold
import com.example.ui.theme.MintAccent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainHomeScreen(
    repository: ChatRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onLockApp: () -> Unit,
    onOpenChat: (conversationId: String) -> Unit,
    onOpenStatusCamera: () -> Unit,
    onOpenQuickCamera: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartCall: (peerName: String, phone: String, avatar: String?, isVideo: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("DISCUSSIONS", "STATUTS", "APPELS")

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showStartCallDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val conversations by repository.getConversations().collectAsState(initial = emptyList())
    val stories by repository.getStories().collectAsState(initial = emptyList())
    val calls by repository.getCalls().collectAsState(initial = emptyList())

    // Selected conversation for action sheet
    var contextMenuConversation by remember { mutableStateOf<ConversationEntity?>(null) }
    var verifySafetyPeer by remember { mutableStateOf<ConversationEntity?>(null) }

    val unreadTotal = remember(conversations) { conversations.sumOf { it.unreadCount } }
    val unseenStoriesCount = remember(stories) { stories.count { !it.isViewed } }

    Scaffold(
        modifier = Modifier.testTag("main_home_scaffold"),
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Rechercher dans Samawakhtane...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Fermer recherche")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Samawakhtane",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "E2EE Actif",
                                tint = AfricanGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(
                            onClick = onOpenQuickCamera,
                            modifier = Modifier.testTag("top_camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Appareil photo", tint = Color.White)
                        }
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = Color.White)
                        }
                        IconButton(onClick = onToggleDarkTheme) {
                            Icon(
                                if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Mode sombre"
                            )
                        }
                        IconButton(onClick = onLockApp) {
                            Icon(Icons.Default.Lock, contentDescription = "Verrouiller l'application")
                        }
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nouvelle discussion") },
                                    leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showNewChatDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Appareil photo") },
                                    leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        onOpenQuickCamera()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Paramètres") },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        onOpenSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Verrouillage biométrique") },
                                    leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        onLockApp()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Chiffrement AES-256 GCM") },
                                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        conversations.firstOrNull()?.let { verifySafetyPeer = it }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showNewChatDialog = true
                        1 -> onOpenStatusCamera()
                        2 -> showStartCallDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("main_fab")
            ) {
                Icon(
                    imageVector = when (selectedTab) {
                        0 -> Icons.Default.Chat
                        1 -> Icons.Default.CameraAlt
                        else -> Icons.Default.AddIcCall
                    },
                    contentDescription = "Action principale"
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                if (index == 0 && unreadTotal > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(containerColor = MintAccent) {
                                        Text(unreadTotal.toString(), color = Color.Black, fontSize = 11.sp)
                                    }
                                }
                                if (index == 1 && unseenStoriesCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MintAccent)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Security assurance pill banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Messages et appels chiffrés de bout en bout (AES-256 GCM & DTLS-SRTP)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    val filteredList = remember(conversations, searchQuery) {
                        if (searchQuery.isBlank()) conversations
                        else conversations.filter {
                            it.peerName.contains(searchQuery, ignoreCase = true) ||
                                    it.lastMessage.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Aucune discussion trouvée",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredList, key = { it.id }) { chat ->
                                ConversationListItem(
                                    conversation = chat,
                                    onClick = { onOpenChat(chat.id) },
                                    onLongClick = { contextMenuConversation = chat }
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 76.dp)
                                )
                            }
                        }
                    }
                }
                1 -> {
                    StatusScreen(
                        stories = stories,
                        onAddStory = onOpenStatusCamera,
                        onStoryViewed = { storyId ->
                            coroutineScope.launch { repository.markStoryViewed(storyId) }
                        }
                    )
                }
                2 -> {
                    CallsScreen(
                        calls = calls,
                        onInitiateCall = { peerName, phone, avatar, isVideo ->
                            onStartCall(peerName, phone, avatar, isVideo)
                        }
                    )
                }
            }
        }
    }

    // Context menu dialog for long-pressed conversation
    contextMenuConversation?.let { conv ->
        AlertDialog(
            onDismissRequest = { contextMenuConversation = null },
            title = { Text(conv.peerName, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.togglePin(conv.id, conv.isPinned)
                                contextMenuConversation = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (conv.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                            contentDescription = null,
                            tint = AfricanGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (conv.isPinned) "Désépingler la discussion" else "Épingler la discussion")
                    }

                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.toggleLock(conv.id, conv.isLocked)
                                contextMenuConversation = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (conv.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (conv.isLocked) "Déverrouiller le chat" else "Verrouiller avec empreinte/PIN")
                    }

                    TextButton(
                        onClick = {
                            val target = contextMenuConversation
                            contextMenuConversation = null
                            target?.let { verifySafetyPeer = it }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MintAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Vérifier le numéro de sécurité E2EE")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { contextMenuConversation = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Safety verification dialog
    verifySafetyPeer?.let { peer ->
        SafetyNumberDialog(
            peerName = peer.peerName,
            peerId = peer.id,
            onDismiss = { verifySafetyPeer = null }
        )
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        var newName by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("+221 ") }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Nouvelle discussion sécurisée", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nom du contact") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Numéro de téléphone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Une clé de chiffrement AES-256 dédiée sera négociée avec ce contact.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            coroutineScope.launch {
                                val id = repository.createNewConversation(newName, newPhone)
                                showNewChatDialog = false
                                onOpenChat(id)
                            }
                        }
                    },
                    enabled = newName.isNotBlank()
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Start Call Dialog
    if (showStartCallDialog) {
        AlertDialog(
            onDismissRequest = { showStartCallDialog = false },
            title = { Text("Démarrer un appel WebRTC", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(conversations) { conv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(url = conv.peerAvatarUrl, seed = conv.peerPhoneNumber, size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(conv.peerName, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                Text(conv.peerPhoneNumber, fontSize = 12.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                showStartCallDialog = false
                                onStartCall(conv.peerName, conv.peerPhoneNumber, conv.peerAvatarUrl, false)
                            }) {
                                Icon(Icons.Default.Call, contentDescription = "Appel audio", tint = MintAccent)
                            }
                            IconButton(onClick = {
                                showStartCallDialog = false
                                onStartCall(conv.peerName, conv.peerPhoneNumber, conv.peerAvatarUrl, true)
                            }) {
                                Icon(Icons.Default.Videocam, contentDescription = "Appel vidéo", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStartCallDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationListItem(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            url = conversation.peerAvatarUrl,
            seed = conversation.peerPhoneNumber,
            size = 52,
            isOnline = conversation.isOnline
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conversation.peerName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conversation.isLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = "Discussion verrouillée",
                            tint = AfricanGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                Text(
                    format.format(Date(conversation.lastTimestamp)),
                    color = if (conversation.unreadCount > 0) MintAccent else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (conversation.isLocked) "🔒 Discussion verrouillée (Toucher pour déverrouiller)" else conversation.lastMessage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.isPinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Épinglé",
                            tint = AfricanGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (conversation.unreadCount > 0) {
                        Badge(containerColor = MintAccent) {
                            Text(
                                conversation.unreadCount.toString(),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
