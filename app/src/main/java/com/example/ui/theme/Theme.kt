package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DrakzoPrimary,
    onPrimary = Color.White,
    primaryContainer = DrakzoPrimaryGlow.copy(alpha = 0.2f),
    onPrimaryContainer = DrakzoPrimaryGlow,
    secondary = DrakzoSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DrakzoSecondaryGlow.copy(alpha = 0.2f),
    onSecondaryContainer = DrakzoSecondaryGlow,
    tertiary = DrakzoTertiary,
    background = DrakzoBg,
    onBackground = DrakzoTextPrimary,
    surface = DrakzoSurface,
    onSurface = DrakzoTextPrimary,
    surfaceVariant = DrakzoSurfaceVariant,
    onSurfaceVariant = DrakzoTextSecondary,
    outline = DrakzoBorder,
    outlineVariant = DrakzoDivider
)

private val LightColorScheme = lightColorScheme(
    primary = DrakzoPrimary,
    onPrimary = Color.White,
    secondary = DrakzoSecondary,
    onSecondary = Color.White,
    background = Color(0xFF0F172A), // Keep Drakzo sleek dark-themed by default for maximum cyber aesthetic
    onBackground = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Drakzo is dark-themed by default for cyber messenger vibe
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
