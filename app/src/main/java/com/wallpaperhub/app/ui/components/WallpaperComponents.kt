package com.wallpaperhub.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wallpaperhub.app.data.local.WallpaperEntity
import com.wallpaperhub.app.ui.theme.WallpaperHubColors

/**
 * 可复用壁纸卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperCard(
    wallpaper: WallpaperEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = WallpaperHubColors.DarkCard),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(wallpaper.thumbnailUrl ?: wallpaper.url)
                .crossfade(true)
                .build(),
            contentDescription = wallpaper.source,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 加载中占位
 */
@Composable
fun LoadingPlaceholder(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = WallpaperHubColors.DarkCard),
        modifier = modifier.fillMaxWidth().aspectRatio(9f / 16f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = WallpaperHubColors.NeonCyan.copy(alpha = 0.5f),
                strokeWidth = 2.dp
            )
        }
    }
}
