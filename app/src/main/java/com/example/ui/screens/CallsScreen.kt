package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecordEntity
import com.example.ui.components.AvatarImage
import com.example.ui.theme.MintAccent
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallsScreen(
    calls: List<CallRecordEntity>,
    onInitiateCall: (peerName: String, phone: String, avatar: String?, isVideo: Boolean) -> Unit
) {
    if (calls.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PhoneCallback,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Aucun appel récent",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Passez des appels audio et vidéo P2P chiffrés",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    "HISTORIQUE DES APPELS CHIFFRÉS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(calls, key = { it.id }) { call ->
                CallRowItem(
                    call = call,
                    onCallBack = {
                        onInitiateCall(call.peerName, call.peerPhoneNumber, call.peerAvatarUrl, call.isVideo)
                    }
                )
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    modifier = Modifier.padding(start = 76.dp)
                )
            }
        }
    }
}

@Composable
fun CallRowItem(
    call: CallRecordEntity,
    onCallBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallBack() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            url = call.peerAvatarUrl,
            seed = call.peerPhoneNumber,
            size = 50
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                call.peerName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (call.isMissed) Color.Red else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when {
                    call.isMissed -> Icons.AutoMirrored.Filled.CallMissed
                    call.isIncoming -> Icons.AutoMirrored.Filled.CallReceived
                    else -> Icons.AutoMirrored.Filled.CallMade
                }
                val iconTint = when {
                    call.isMissed -> Color.Red
                    call.isIncoming -> MintAccent
                    else -> Color(0xFF2196F3)
                }

                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                Text(
                    format.format(Date(call.timestamp)),
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                if (call.durationSec > 0) {
                    val min = call.durationSec / 60
                    val sec = call.durationSec % 60
                    Text(
                        " • (%02d:%02d)".format(min, sec),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        IconButton(onClick = onCallBack) {
            Icon(
                imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Rappeler",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
