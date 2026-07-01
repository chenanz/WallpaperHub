package com.wallpaperhub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wallpaperhub.app.data.local.AppDatabase
import com.wallpaperhub.app.data.local.WallpaperEntity
import com.wallpaperhub.app.data.repository.WallpaperRepository
import com.wallpaperhub.app.ui.theme.WallpaperHubColors
import com.wallpaperhub.app.util.WallpaperSetter
import kotlinx.coroutines.launch

/**
 * 全屏预览 - 支持设为壁纸
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(wallpaperId: String, navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = AppDatabase.getInstance(context).wallpaperDao()

    var wallpaper by remember { mutableStateOf<WallpaperEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(wallpaperId) {
        wallpaper = dao.getWallpaperById(wallpaperId)
        isFavorite = wallpaper?.isFavorite ?: false
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
                containerColor = WallpaperHubColors.DarkSurface,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 收藏按钮
                    IconButton(onClick = {
                        scope.launch {
                            dao.updateFavorite(wallpaperId, !isFavorite)
                            isFavorite = !isFavorite
                        }
                    }) {
                        Text(
                            if (isFavorite) "❤️" else "🤍",
                            fontSize = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )
                    }

                    // 设为壁纸按钮
                    Button(
                        onClick = { showMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WallpaperHubColors.NeonPurple
                        )
                    ) {
                        Text("🎨 设为壁纸", fontWeight = FontWeight.Bold)
                    }

                    // 下载按钮
                    IconButton(onClick = {
                        // TODO: 下载到本地
                    }) {
                        Text("⬇️", fontSize = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp))
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentWallpaper.url)
                    .crossfade(true)
                    .build(),
                contentDescription = "wallpaper preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // 来源信息
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp, padding.calculateBottomPadding())
            ) {
                Text(
                    "来源: ${currentWallpaper.source}",
                    color = WallpaperHubColors.DarkText.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Light
                )
                currentWallpaper.resolution?.let {
                    Text(
                        "分辨率: $it",
                        color = WallpaperHubColors.DarkText.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }

        // 壁纸设置菜单
        if (showMenu) {
            AlertDialog(
                onDismissRequest = { showMenu = false },
                title = { Text("设置壁纸") },
                text = {
                    Column {
                        TextButton(onClick = {
                            scope.launch {
                                WallpaperSetter.setWallpaper(context, currentWallpaper.url, "home")
                            }
                            showMenu = false
                        }) { Text("🏠 主屏幕") }

                        TextButton(onClick = {
                            scope.launch {
                                WallpaperSetter.setWallpaper(context, currentWallpaper.url, "lock")
                            }
                            showMenu = false
                        }) { Text("🔒 锁屏") }

                        TextButton(onClick = {
                            scope.launch {
                                WallpaperSetter.setWallpaper(context, currentWallpaper.url, "both")
                            }
                            showMenu = false
                        }) { Text("📱 同时设置") }
                    }
                },
                confirmButton = {},
                containerColor = WallpaperHubColors.DarkSurface,
            )
        }
    }
}
