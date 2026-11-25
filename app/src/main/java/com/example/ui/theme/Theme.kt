package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuminaDarkColorScheme = darkColorScheme(
    primary = LuminaCyan,
    onPrimary = ObsidianDeep,
    primaryContainer = Color(0xFF15344A),
    onPrimaryContainer = Color(0xFFC9F3FF),
    secondary = LuminaCobalt,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1A2948),
    onSecondaryContainer = Color(0xFFD5E3FF),
    tertiary = LuminaMint,
    onTertiary = ObsidianDeep,
    background = ObsidianDeep,
    onBackground = InkPrimaryLight,
    surface = ObsidianSurface,
    onSurface = InkPrimaryLight,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = InkSecondaryLight,
    outline = GlassBorderLight,
    outlineVariant = GlassBorderSubtle,
    error = LuminaCoral,
    onError = Color.White
)

@Composable
fun LuminaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LuminaDarkColorScheme,
        typography = LuminaTypography,
        content = content
    )
}
