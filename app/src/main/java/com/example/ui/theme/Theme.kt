package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MintAccent,
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005C4B),
    onPrimaryContainer = Color(0xFF79F8C4),
    secondary = AfricanGold,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = EmeraldMedium,
    background = DarkBackground,
    onBackground = Color(0xFFE9EDEF),
    surface = DarkSurface,
    onSurface = Color(0xFFE9EDEF),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF8696A0)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F4EE),
    onPrimaryContainer = Color(0xFF002018),
    secondary = AfricanGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0C4),
    onSecondaryContainer = Color(0xFF261900),
    tertiary = EmeraldMedium,
    background = LightBackground,
    onBackground = Color(0xFF111B21),
    surface = LightSurface,
    onSurface = Color(0xFF111B21),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF667781)
)

@Composable
fun SamawakhtaneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
