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

/**
 * 收藏页面 - 只显示普通壁纸的收藏
 * R18 收藏在隔离视图
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember { WallpaperRepository(context) }

    var favorites by remember { mutableStateOf<List<WallpaperEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.getFavorites().collect { list ->
            favorites = list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "❤️ 收藏",
                        color = WallpaperHubColors.NeonPink,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WallpaperHubColors.DarkSurface
                )
            )
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无收藏", color = WallpaperHubColors.DarkTextSecondary)
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
                items(favorites, key = { it.id }) { wallpaper ->
                    WallpaperGridItem(wallpaper = wallpaper) {
                        navController.navigate(Routes.previewRoute(wallpaper.id))
                    }
                }
            }
        }
    }
}
