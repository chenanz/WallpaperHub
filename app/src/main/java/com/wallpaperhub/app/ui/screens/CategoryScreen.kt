package com.wallpaperhub.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wallpaperhub.app.ui.theme.WallpaperHubColors

/**
 * 分类页面 - 三个 Tab
 * 1. 静态壁纸
 * 2. Live Photo (2.5D 动效)
 * 3. 3D 渲染
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(navController: NavController) {
    val tabTitles = listOf("静态壁纸", "Live Photo", "3D 渲染")
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📂 分类",
                        color = WallpaperHubColors.NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WallpaperHubColors.DarkSurface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = WallpaperHubColors.DarkSurface,
                contentColor = WallpaperHubColors.NeonCyan,
                edgePadding = 16.dp,
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            // 切换 Tab
                        },
                        text = {
                            Text(
                                title,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (pagerState.currentPage == index)
                                    WallpaperHubColors.NeonCyan
                                else
                                    WallpaperHubColors.DarkTextSecondary
                            )
                        }
                    )
                }
            }

            // Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> StaticWallpaperTab(navController)
                    1 -> LivePhotoTab(navController)
                    2 -> Render3DTab(navController)
                }
            }
        }
    }
}

@Composable
fun StaticWallpaperTab(navController: NavController) {
    // 静态壁纸 - 复用 HomeScreen 的网格布局逻辑
    // 按 source 分组展示
    CategoryWallpaperList(navController, filterTags = listOf("wallpaper", "static"))
}

@Composable
fun LivePhotoTab(navController: NavController) {
    // Live Photo / 2.5D 动效壁纸
    // Glide 加载动效图
    CategoryWallpaperList(navController, filterTags = listOf("live_photo", "2.5d", "animated"))
}

@Composable
fun Render3DTab(navController: NavController) {
    // 3D 渲染壁纸
    CategoryWallpaperList(navController, filterTags = listOf("3d", "render", "cgi"))
}

@Composable
private fun CategoryWallpaperList(
    navController: NavController,
    filterTags: List<String>
) {
    // 复用网格布局，过滤特定标签
    // 实际实现可复用 HomeScreen 的 WallpaperGridItem
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            "按标签过滤: ${filterTags.joinToString(", ")}",
            color = WallpaperHubColors.DarkTextSecondary
        )
        // TODO: 接入 Repository 按标签查询
    }
}
