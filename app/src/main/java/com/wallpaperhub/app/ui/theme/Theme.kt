package com.wallpaperhub.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 暗色科技风配色
object WallpaperHubColors {
    val NeonPurple = Color(0xFFBB86FC)
    val NeonCyan = Color(0xFF03DAC5)
    val NeonPink = Color(0xFFFF0266)
    val DarkBg = Color(0xFF0A0A0F)
    val DarkSurface = Color(0xFF12121A)
    val DarkCard = Color(0xFF1A1A2E)
    val DarkText = Color(0xFFE0E0E0)
    val DarkTextSecondary = Color(0xFF888899)
}

@Composable
fun WallpaperHubTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = WallpaperHubColors.NeonPurple,
        secondary = WallpaperHubColors.NeonCyan,
        tertiary = WallpaperHubColors.NeonPink,
        background = WallpaperHubColors.DarkBg,
        surface = WallpaperHubColors.DarkSurface,
        surfaceVariant = WallpaperHubColors.DarkCard,
        onBackground = WallpaperHubColors.DarkText,
        onSurface = WallpaperHubColors.DarkText,
        onSurfaceVariant = WallpaperHubColors.DarkTextSecondary,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
