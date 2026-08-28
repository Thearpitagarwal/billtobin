package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = MinimalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = MinimalBlueContainer,
    onPrimaryContainer = MinimalOnBlueContainer,
    secondary = Slate700,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    background = MinimalBg,
    onBackground = Slate900,
    surface = MinimalSurface,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = OfflineAlertRed,
    onError = Color.White,
    errorContainer = OfflineAlertBg,
    onErrorContainer = OfflineAlertRed
)

private val DarkColorScheme = darkColorScheme(
    primary = MinimalBlueContainer,
    onPrimary = MinimalOnBlueContainer,
    primaryContainer = MinimalBlueDark,
    onPrimaryContainer = Color.White,
    secondary = Slate300,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = Color.White,
    background = Slate900,
    onBackground = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate500,
    outlineVariant = Slate700,
    error = Color(0xFFFF8389),
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
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
