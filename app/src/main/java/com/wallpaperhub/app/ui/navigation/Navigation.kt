package com.wallpaperhub.app.ui.navigation

import android.util.Base64
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wallpaperhub.app.ui.screens.*
import com.wallpaperhub.app.ui.theme.WallpaperHubColors

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val PREVIEW = "preview/{wallpaperId}"
    const val LIVE_PREVIEW = "live_preview/{wallpaperId}"

    // R18 路由 - Base64 编码防反编译
    // "R18Gallery" -> Base64 = "UjE4R2FsbGVyeQ=="
    private const val R18_BASE = "UjE4R2FsbGVyeQ=="
    val R18_GALLERY: String = String(Base64.decode(R18_BASE, Base64.DEFAULT))

    fun previewRoute(id: String) = "preview/$id"
    fun livePreviewRoute(id: String) = "live_preview/$id"
}

sealed class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home : BottomNavItem(Routes.HOME, "首页", Icons.Filled.Home)
    data object Category : BottomNavItem(Routes.CATEGORY, "分类", Icons.Filled.Category)
    data object Favorites : BottomNavItem(Routes.FAVORITES, "收藏", Icons.Filled.Favorite)
    data object Settings : BottomNavItem(Routes.SETTINGS, "设置", Icons.Filled.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperHubNavHost() {
    val navController = rememberNavController()
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Category,
        BottomNavItem.Favorites,
        BottomNavItem.Settings,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // R18 页面不显示底部导航
    val showBottomBar = currentDestination?.route != Routes.R18_GALLERY

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = WallpaperHubColors.DarkSurface,
                    contentColor = WallpaperHubColors.NeonCyan,
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WallpaperHubColors.NeonCyan,
                                selectedTextColor = WallpaperHubColors.NeonCyan,
                                indicatorColor = WallpaperHubColors.DarkCard,
                                unselectedIconColor = WallpaperHubColors.DarkTextSecondary,
                                unselectedTextColor = WallpaperHubColors.DarkTextSecondary,
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) { HomeScreen(navController) }
            composable(Routes.CATEGORY) { CategoryScreen(navController) }
            composable(Routes.FAVORITES) { FavoritesScreen(navController) }
            composable(Routes.SETTINGS) { SettingsScreen(navController) }
            composable(Routes.PREVIEW) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("wallpaperId") ?: ""
                if (id.isNotEmpty()) PreviewScreen(wallpaperId = id, navController = navController)
            }
            composable(Routes.LIVE_PREVIEW) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("wallpaperId") ?: ""
                if (id.isNotEmpty()) LivePreviewScreen(wallpaperId = id, navController = navController)
            }
            // 隐藏路由 - 动态拼接
            composable(Routes.R18_GALLERY) { R18GalleryScreen(navController) }
        }
    }
}
