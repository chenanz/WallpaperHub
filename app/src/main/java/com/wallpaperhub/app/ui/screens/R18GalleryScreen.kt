package com.wallpaperhub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wallpaperhub.app.data.local.WallpaperEntity
import com.wallpaperhub.app.data.repository.WallpaperRepository
import com.wallpaperhub.app.ui.navigation.Routes
import com.wallpaperhub.app.ui.theme.WallpaperHubColors
import com.wallpaperhub.app.util.ScreenshotBlocker
import com.wallpaperhub.app.util.SecretUtils
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf

/**
 * R18 画廊 - 完全隔离
 * 1. 不在推荐、搜索、分类中出现
 * 2. 只加载 is_r18=true 的数据
 * 3. FLAG_SECURE 阻止截屏
 * 4. 使用隔离的 Coil 缓存目录 cache/r18
 * 5. 离开时移除 FLAG_SECURE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun R18GalleryScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { WallpaperRepository(context) }
    val scope = rememberCoroutineScope()

    // 验证是否已解锁
    if (!SecretUtils.isUnlocked) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    // 截屏防护
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        ScreenshotBlocker.enable((context as androidx.activity.ComponentActivity).window)
        onDispose {
            ScreenshotBlocker.disable((context as androidx.activity.ComponentActivity).window)
            view.keepScreenOn = false
            SecretUtils.lock()
        }
    }

    var wallpapers by remember { mutableStateOf<List<WallpaperEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // 只加载 R18 数据
        repository.getR18Wallpapers().collect { list ->
            wallpapers = list
            isLoading = false
        }
        // 后台刷新 R18
        scope.launch {
            repository.refreshR18Wallpapers(1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🔐 隐藏画廊",
                        color = WallpaperHubColors.NeonPink,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WallpaperHubColors.DarkSurface
                ),
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("← 返回", color = WallpaperHubColors.DarkTextSecondary)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && wallpapers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WallpaperHubColors.NeonPink)
            }
        } else if (wallpapers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无内容", color = WallpaperHubColors.DarkTextSecondary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 4.dp, end = 4.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(wallpapers, key = { it.id }) { wallpaper ->
                    WallpaperGridItem(wallpaper = wallpaper) {
                        navController.navigate(Routes.previewRoute(wallpaper.id))
                    }
                }
            }
        }
    }
}
