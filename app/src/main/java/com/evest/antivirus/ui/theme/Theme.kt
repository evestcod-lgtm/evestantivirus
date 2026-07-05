package com.evest.antivirus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EvestDarkScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = ObsidianBlack,
    secondary = EmberOrange,
    onSecondary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = EmberOrangeSoft,
    surface = ObsidianSurface,
    onSurface = EmberOrangeSoft,
    surfaceVariant = ObsidianSurfaceElevated,
    error = DangerRed,
    onError = ObsidianBlack
)

@Composable
fun EvestAntivirusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = ObsidianBlack.toArgb()
            it.navigationBarColor = ObsidianBlack.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = EvestDarkScheme,
        typography = EvestTypography,
        content = content
    )
}
