package com.wallpaperhub.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wallpaperhub.app.data.local.WallpaperEntity
import com.wallpaperhub.app.data.repository.WallpaperRepository
import com.wallpaperhub.app.ui.navigation.Routes
import com.wallpaperhub.app.ui.theme.WallpaperHubColors
import kotlinx.coroutines.launch

/**
 * 首页 - 普通壁纸网格
 * 每行3列，显示缩略图
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { WallpaperRepository(context) }
    val scope = rememberCoroutineScope()

    var wallpapers by remember { mutableStateOf<List<WallpaperEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        // 先读本地缓存
        repository.getNormalWallpapers().collect { localList ->
            wallpapers = localList
            isLoading = false
        }
        // 后台刷新
        scope.launch {
            repository.refreshNormalWallpapers(page = 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🏠 壁纸",
                        color = WallpaperHubColors.NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WallpaperHubColors.DarkSurface,
                )
            )
        }
    ) { padding ->
        if (isLoading && wallpapers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WallpaperHubColors.NeonCyan)
            }
        } else if (wallpapers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无壁纸", color = WallpaperHubColors.DarkTextSecondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                repository.refreshNormalWallpapers(1)
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WallpaperHubColors.NeonPurple
                        )
                    ) {
                        Text("刷新")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 4.dp, end = 4.dp, top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperGridItem(wallpaper = wallpaper) {
                        navController.navigate(Routes.previewRoute(wallpaper.id))
                    }
                }

                // 加载更多
                item {
                    LaunchedEffect(Unit) {
                        scope.launch {
                            page++
                            repository.refreshNormalWallpapers(page)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperGridItem(
    wallpaper: WallpaperEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)  // 竖屏壁纸比例
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = WallpaperHubColors.DarkCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(wallpaper.thumbnailUrl ?: wallpaper.url)
                    .crossfade(true)
                    .build(),
                contentDescription = wallpaper.source,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 来源标签
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp),
                color = WallpaperHubColors.DarkBg.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = wallpaper.source,
                    color = WallpaperHubColors.DarkTextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
