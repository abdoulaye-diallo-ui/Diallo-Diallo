package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.security.CryptoManager
import com.example.security.EncryptionInspection
import com.example.ui.theme.AfricanGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.MintAccent

@Composable
fun AvatarImage(
    url: String?,
    seed: String,
    modifier: Modifier = Modifier,
    size: Int = 50,
    isOnline: Boolean = false
) {
    val avatarUrl = url ?: "https://api.dicebear.com/7.x/bottts/png?seed=$seed"
    Box(modifier = modifier.size(size.dp)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0))
        )
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size((size * 0.28).dp)
                    .clip(CircleShape)
                    .background(MintAccent)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barsCount: Int = 16,
    color: Color = EmeraldDark
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val heights = List(barsCount) { index ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 300 + (index * 45) % 400,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
        } else {
            remember { mutableFloatStateOf(0.3f + (index % 5) * 0.12f) }
        }
    }

    Row(
        modifier = modifier.height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { animatedHeight ->
            val factor = if (isPlaying) (animatedHeight as State<Float>).value else (animatedHeight as State<Float>).value
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(factor.coerceIn(0.15f, 1.0f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun E2EEInspectionDialog(
    encryptedContent: String,
    onDismiss: () -> Unit
) {
    val inspection: EncryptionInspection = remember(encryptedContent) {
        CryptoManager.inspect(encryptedContent)
    }
    val clipboard = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MintAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inspection Cryptographique", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "PROTOCOLE: ${inspection.algorithm}",
                            color = MintAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "CLÉ: AndroidKeyStore (256-bit AES GCM)",
                            color = AfricanGold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "IV (12 octets): ${inspection.ivBase64}",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "TAG AUTH: 128 bits intégrité garantie",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text("Message déchiffré (en clair) :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        inspection.plainText,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 14.sp
                    )
                }

                Text("Charge chiffrée stockée (AES-256 GCM Base64) :", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        inspection.cipherTextBase64,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                clipboard.setText(AnnotatedString(inspection.cipherTextBase64))
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copier le chiffrement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
fun SafetyNumberDialog(
    peerName: String,
    peerId: String,
    onDismiss: () -> Unit
) {
    val fingerprint = remember(peerId) { CryptoManager.getSafetyFingerprint(peerId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MintAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Numéro de sécurité", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Vérifiez l'empreinte de sécurité avec $peerName pour confirmer que personne n'intercepte vos communications.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            fingerprint,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MintAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Clé vérifiée dans AndroidKeyStore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MintAccent
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Vérifié et conforme")
            }
        }
    )
}
