package com.brewmaster.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BrewMasterColorScheme = darkColorScheme(
    primary = LimeGreen,
    onPrimary = DarkBackground,
    primaryContainer = LimeGreenDark,
    onPrimaryContainer = LimeGreenLight,
    secondary = IceBlue,
    onSecondary = DarkBackground,
    tertiary = HotOrange,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = DarkBackground
)

@Composable
fun BrewMasterTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = BrewMasterColorScheme,
        typography = BrewTypography,
        content = content
    )
}
