package com.inkr8.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Inkr8 Dark Color Scheme
 * primary -> Branding Gold
 * secondary -> Standard Surface
 * tertiary -> Lighter Surface / Highlights
 * background -> Deep Black Background
 * surface -> Standard Surface
 */
private val DarkColorScheme = darkColorScheme(
    primary = Inkr8Gold,
    onPrimary = Color.Black,
    secondary = Inkr8Surface,
    onSecondary = Color.White,
    tertiary = Inkr8SurfaceLight,
    onTertiary = Color.White,
    background = Inkr8Background,
    onBackground = Color.White,
    surface = Inkr8Surface,
    onSurface = Color.White,
    surfaceVariant = Inkr8SurfaceDark,
    onSurfaceVariant = Color.Gray,
    error = Inkr8Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Inkr8Gold,
    onPrimary = Color.Black,
    secondary = Color.White,
    onSecondary = Color.Black,
    tertiary = Color(0xFFF5F5F5),
    onTertiary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun Inkr8Theme(
    darkTheme: Boolean = true, // Force dark theme for Inkr8's aesthetic by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
