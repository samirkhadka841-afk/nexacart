package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexaColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberHighlight,
    background = CyberDarkBg,
    surface = CyberSurface,
    onPrimary = Color.Black,          // High contrast dark text on hot cyan
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = CyberSurfaceLighter,
    onSurfaceVariant = Color.White,
    error = Color(0xFFFF4D4D)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = Color(0xFFAFB9C8),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF07090E),
    onSurface = Color(0xFF07090E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark of NexaCart visually
    dynamicColor: Boolean = false, // Overridden to match NexaCart's bespoke brand signature
    content: @Composable () -> Unit,
) {
    // NexaCart demands custom high-fidelity cyber branding regardless of device theme
    val colorScheme = if (darkTheme) NexaColorScheme else NexaColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
