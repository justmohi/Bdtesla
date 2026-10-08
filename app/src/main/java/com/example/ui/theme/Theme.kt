package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TeslaGreenNeon,
    onPrimary = TeslaDarkBg,
    primaryContainer = TeslaGreenDark,
    onPrimaryContainer = TeslaDarkTextPrimary,
    secondary = TeslaCyanAccent,
    onSecondary = TeslaDarkBg,
    tertiary = TeslaGoldAccent,
    background = TeslaDarkBg,
    onBackground = TeslaDarkTextPrimary,
    surface = TeslaDarkSurface,
    onSurface = TeslaDarkTextPrimary,
    surfaceVariant = TeslaDarkCard,
    onSurfaceVariant = TeslaDarkTextSecondary,
    outline = TeslaDarkCardBorder,
    error = StatusDanger,
    onError = TeslaDarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = TeslaGreenPrimary,
    onPrimary = TeslaLightSurface,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF0891B2),
    onSecondary = TeslaLightSurface,
    tertiary = Color(0xFFD97706),
    background = TeslaLightBg,
    onBackground = TeslaLightTextPrimary,
    surface = TeslaLightSurface,
    onSurface = TeslaLightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TeslaLightTextSecondary,
    outline = TeslaLightCardBorder,
    error = StatusDanger,
    onError = TeslaLightSurface
)

@Composable
fun BdTeslaTheme(
    darkTheme: Boolean = true, // Default to sleek modern dark mode for electric BD TESLA aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
