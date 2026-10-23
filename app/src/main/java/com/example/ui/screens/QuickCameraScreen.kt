package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.ConversationEntity
import com.example.ui.components.AvatarImage
import com.example.ui.theme.AfricanGold
import com.example.ui.theme.MintAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val CameraPresetScenes = listOf(
    Pair("Vue Dakar", "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?auto=format&fit=crop&w=800&q=80"),
    Pair("Plage Teranga", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80"),
    Pair("Marché Artisanal", "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=800&q=80"),
    Pair("Coucher de Soleil", "https://images.unsplash.com/photo-1516026672322-bc52d61a55d5?auto=format&fit=crop&w=800&q=80")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCameraScreen(
    conversations: List<ConversationEntity>,
    onClose: () -> Unit,
    onPostToStory: (caption: String, imageUrl: String) -> Unit,
    onSendToChat: (conversationId: String, caption: String, imageUrl: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isFrontCamera by remember { mutableStateOf(false) }
    var flashMode by remember { mutableIntStateOf(0) } // 0: Auto, 1: On, 2: Off
    var selectedSceneIndex by remember { mutableIntStateOf(0) }
    var isPhotoCaptured by remember { mutableStateOf(false) }
    var flashAnimationTrigger by remember { mutableStateOf(false) }
    var captionText by remember { mutableStateOf("") }
    var selectedRecipientId by remember { mutableStateOf<String?>(null) }
    var showSendTargetDialog by remember { mutableStateOf(false) }

    val currentImageUrl = CameraPresetScenes[selectedSceneIndex].second

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("quick_camera_screen")
    ) {
        // Camera Viewfinder Background
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(currentImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Viseur Caméra",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Flash shutter overlay
        AnimatedVisibility(
            visible = flashAnimationTrigger,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        // Top controls: Close, Flash, Switch Camera, Scenes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
            }

            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    IconButton(
                        onClick = { flashMode = (flashMode + 1) % 3 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        val flashIcon = when (flashMode) {
                            0 -> Icons.Default.FlashAuto
                            1 -> Icons.Default.FlashOn
                            else -> Icons.Default.FlashOff
                        }
                        Icon(flashIcon, contentDescription = "Flash", tint = if (flashMode == 1) AfricanGold else Color.White)
                    }

                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Changer caméra", tint = Color.White)
                    }
                }
            }
        }

        // Camera Scene Selector / Filters
        if (!isPhotoCaptured) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    "Sélecteur de scène & environnement",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CameraPresetScenes.indices.toList()) { index ->
                        val scene = CameraPresetScenes[index]
                        val isSelected = selectedSceneIndex == index
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MintAccent else Color.Black.copy(alpha = 0.6f),
                            border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable { selectedSceneIndex = index }
                        ) {
                            Text(
                                text = scene.first,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Shutter row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery shortcut
                    IconButton(
                        onClick = {
                            selectedSceneIndex = (selectedSceneIndex + 1) % CameraPresetScenes.size
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Galerie", tint = Color.White)
                    }

                    // Large Shutter Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                coroutineScope.launch {
                                    flashAnimationTrigger = true
                                    delay(150)
                                    flashAnimationTrigger = false
                                    isPhotoCaptured = true
                                }
                            }
                            .testTag("camera_shutter_button")
                    )

                    // Switch camera shortcut
                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Inverser", tint = Color.White)
                    }
                }
            }
        } else {
            // Post-Capture Edit & Share Mode
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
                    .padding(16.dp)
            ) {
                // Caption text input
                TextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { Text("Ajouter une légende... 🇸🇳", color = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = {
                        IconButton(onClick = { captionText += " 🇸🇳" }) {
                            Icon(Icons.Default.Mood, contentDescription = "Emoji", tint = Color.White)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share to Status or Send to Chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val caption = captionText.ifBlank { "Statut pris avec la Caméra Samawakhtane" }
                            onPostToStory(caption, currentImageUrl)
                            Toast.makeText(context, "Publié dans votre Statut !", Toast.LENGTH_SHORT).show()
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintAccent),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mon Statut (24h)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showSendTargetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Envoyer à...", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Recipient selection dialog
    if (showSendTargetDialog) {
        AlertDialog(
            onDismissRequest = { showSendTargetDialog = false },
            title = { Text("Envoyer la photo à...", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Choisissez la discussion pour envoyer cette photo chiffrée AES-256 :",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    conversations.forEach { conv ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val caption = captionText.ifBlank { "Photo" }
                                    onSendToChat(conv.id, caption, currentImageUrl)
                                    showSendTargetDialog = false
                                    Toast.makeText(context, "Photo envoyée à ${conv.peerName} !", Toast.LENGTH_SHORT).show()
                                    onClose()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(url = conv.peerAvatarUrl, seed = conv.peerPhoneNumber, size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(conv.peerName, fontWeight = FontWeight.Medium)
                                Text(conv.peerPhoneNumber, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSendTargetDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
