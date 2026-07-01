package com.wallpaperhub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wallpaperhub.app.data.local.AppDatabase
import com.wallpaperhub.app.data.local.WallpaperEntity
import com.wallpaperhub.app.ui.theme.WallpaperHubColors
import com.wallpaperhub.app.util.WallpaperSetter
import kotlinx.coroutines.launch

/**
 * Live Photo / 动效壁纸预览
 * Glide 处理动效壁纸播放
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePreviewScreen(wallpaperId: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = AppDatabase.getInstance(context).wallpaperDao()

    var wallpaper by remember { mutableStateOf<WallpaperEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(wallpaperId) {
        wallpaper = dao.getWallpaperById(wallpaperId)
    }

    val currentWallpaper = wallpaper

    if (currentWallpaper == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = WallpaperHubColors.NeonCyan)
        }
        return
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = WallpaperHubColors.DarkSurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 设置为动态壁纸
                    Button(
                        onClick = { showMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WallpaperHubColors.NeonPurple
                        )
                    ) {
                        Text("🎬 设为动态壁纸", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // TODO: Glide 加载动效壁纸（视频/GIF/WebP 动画）
            // 使用 Glide 的 compose 集成
            Text(
                "Live Photo 预览\n${currentWallpaper.url}",
                color = WallpaperHubColors.DarkTextSecondary
            )
        }

        if (showMenu) {
            AlertDialog(
                onDismissRequest = { showMenu = false },
                title = { Text("设置动态壁纸") },
                text = {
                    Column {
                        Text("提示: 动态壁纸需要通过系统壁纸服务设置")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentWallpaper.source, color = WallpaperHubColors.DarkTextSecondary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMenu = false }) {
                        Text("知道了", color = WallpaperHubColors.NeonCyan)
                    }
                },
                containerColor = WallpaperHubColors.DarkSurface,
            )
        }
    }
}
