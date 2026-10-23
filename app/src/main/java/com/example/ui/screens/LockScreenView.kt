package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AfricanGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.MintAccent

@Composable
fun LockScreenView(
    title: String = "Samawakhtane Sécurisé",
    subtitle: String = "Authentifiez-vous par biométrie ou saisissez votre code PIN (par défaut : 1234)",
    onBiometricClick: () -> Unit,
    onPinSuccess: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val onDigitClick: (String) -> Unit = { digit ->
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            pinError = false
            if (newPin.length == 4) {
                if (newPin == "1234") {
                    onPinSuccess()
                } else {
                    pinError = true
                    enteredPin = ""
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("lock_screen_view"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                subtitle,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val filled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    pinError -> Color.Red
                                    filled -> MintAccent
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                }
                            )
                    )
                }
            }

            if (pinError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Code PIN incorrect. Essayez 1234", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Numeric Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("FINGERPRINT", "0", "BACK")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "FINGERPRINT" -> {
                                    IconButton(
                                        onClick = onBiometricClick,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Icon(
                                            Icons.Default.Fingerprint,
                                            contentDescription = "Empreinte",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                "BACK" -> {
                                    IconButton(
                                        onClick = {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        },
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Backspace,
                                            contentDescription = "Effacer",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { onDigitClick(key) },
                                        modifier = Modifier.size(60.dp),
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = onBiometricClick,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Utiliser l'empreinte digitale")
            }

            if (onCancel != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onCancel) {
                    Text("Annuler")
                }
            }
        }
    }
}
