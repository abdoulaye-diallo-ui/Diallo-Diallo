package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.StoryEntity
import com.example.ui.components.AvatarImage
import com.example.ui.theme.AfricanGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.MintAccent
import java.text.SimpleDateFormat
import java.util.*

val StoryGradients = listOf(
    Brush.verticalGradient(listOf(Color(0xFF075E54), Color(0xFF128C7E))),
    Brush.verticalGradient(listOf(Color(0xFFD4AF37), Color(0xFFE65100))),
    Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))),
    Brush.verticalGradient(listOf(Color(0xFF831843), Color(0xFFBE185D)))
)

@Composable
fun StatusScreen(
    stories: List<StoryEntity>,
    onAddStory: () -> Unit,
    onStoryViewed: (String) -> Unit
) {
    var activeStoryToView by remember { mutableStateOf<StoryEntity?>(null) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // "Mon statut"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddStory() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AvatarImage(
                        url = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                        seed = "my_user",
                        size = 54
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MintAccent)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("Mon statut", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Touchez pour ajouter une actualité (24h)", color = Color.Gray, fontSize = 13.sp)
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            Text(
                "RÉCENTES MISES À JOUR",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        items(stories, key = { it.id }) { story ->
            StoryRowItem(
                story = story,
                onClick = {
                    onStoryViewed(story.id)
                    activeStoryToView = story
                }
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                modifier = Modifier.padding(start = 76.dp)
            )
        }
    }

    activeStoryToView?.let { story ->
        StoryViewerDialog(
            story = story,
            onDismiss = { activeStoryToView = null }
        )
    }
}

@Composable
fun StoryRowItem(
    story: StoryEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val ringColor = if (story.isViewed) Color.LightGray else MintAccent
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.5.dp, ringColor, CircleShape)
                .padding(3.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(story.userAvatarUrl ?: "https://api.dicebear.com/7.x/bottts/png?seed=${story.userName}")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(story.userName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            Text(
                "Aujourd'hui à ${format.format(Date(story.timestamp))}",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun StoryViewerDialog(
    story: StoryEntity,
    onDismiss: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(story.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            // Media or Text
            if (!story.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(story.mediaUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Statut",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val brush = StoryGradients.getOrElse(story.bgGradientIndex) { StoryGradients[0] }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        story.textContent ?: "",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                }
            }

            // Progress bar and top bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 12.dp, end = 12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(
                        url = story.userAvatarUrl,
                        seed = story.userName,
                        size = 36
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(story.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        Text(format.format(Date(story.timestamp)), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }
            }

            // Caption at bottom
            if (story.caption.isNotBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = story.caption,
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
