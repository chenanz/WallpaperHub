package com.wallpaperhub.app.data.repository

import android.content.Context
import android.util.Log
import com.wallpaperhub.app.data.api.ApiClient
import com.wallpaperhub.app.data.local.AppDatabase
import com.wallpaperhub.app.data.local.WallpaperEntity
import com.wallpaperhub.app.data.model.WallpaperDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 壁纸仓库 - 协调远程 API 和本地 Room 数据库
 * 先读本地缓存，后台刷新
 */
class WallpaperRepository(private val context: Context) {

    private val api = ApiClient.api
    private val dao = AppDatabase.getInstance(context).wallpaperDao()

    // === 普通壁纸 ===

    fun getNormalWallpapers(): Flow<List<WallpaperEntity>> = dao.getNormalWallpapers()

    suspend fun refreshNormalWallpapers(page: Int = 1, limit: Int = 20) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getNormalWallpapers(page, limit)
                val entities = response.wallpapers.map { it.toEntity() }
                dao.insertAll(entities)
            } catch (e: Exception) {
                Log.e("WallpaperRepo", "刷新普通壁纸失败: ${e.message}")
            }
        }
    }

    // === R18 壁纸 ===

    fun getR18Wallpapers(): Flow<List<WallpaperEntity>> = dao.getR18Wallpapers()

    suspend fun refreshR18Wallpapers(page: Int = 1, limit: Int = 20) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.getR18Wallpapers(page, limit)
                val entities = response.wallpapers.map { it.toEntity() }
                dao.insertAll(entities)
            } catch (e: Exception) {
                Log.e("WallpaperRepo", "刷新R18壁纸失败: ${e.message}")
            }
        }
    }

    // === 收藏 ===

    fun getFavorites(): Flow<List<WallpaperEntity>> = dao.getFavorites()

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateFavorite(id, isFavorite)
        }
    }

    // === 全量同步 ===

    suspend fun syncFromServer() {
        withContext(Dispatchers.IO) {
            try {
                var page = 1
                val limit = 50
                // 拉取普通壁纸
                while (true) {
                    val response = api.getNormalWallpapers(page, limit)
                    val entities = response.wallpapers.map { it.toEntity() }
                    dao.insertAll(entities)
                    if (page >= (response.pages ?: 1)) break
                    page++
                }

                // 拉取 R18 壁纸（如果密钥已配置）
                page = 1
                try {
                    while (true) {
                        val response = api.getR18Wallpapers(page, limit)
                        val entities = response.wallpapers.map { it.toEntity() }
                        dao.insertAll(entities)
                        if (page >= (response.pages ?: 1)) break
                        page++
                    }
                } catch (e: Exception) {
                    Log.w("WallpaperRepo", "R18同步跳过（可能未配置密钥）: ${e.message}")
                }

                Log.i("WallpaperRepo", "同步完成")
            } catch (e: Exception) {
                Log.e("WallpaperRepo", "同步失败: ${e.message}")
            }
        }
    }

    // === 缓存管理 ===

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            dao.clearUnfavoritedUnCached()
            // 清除图片缓存
            context.cacheDir.resolve("image_cache").deleteRecursively()
        }
    }

    // === 转换工具 ===

    private fun WallpaperDto.toEntity(): WallpaperEntity = WallpaperEntity(
        id = id,
        url = url,
        thumbnailUrl = thumbnailUrl,
        source = source,
        tags = tags ?: emptyList(),
        resolution = resolution,
        isR18 = isR18,
        isFavorite = false,
        localPath = null,
        createdAt = createdAt
    )

    companion object {
        private const val TAG = "WallpaperRepository"
    }
}
