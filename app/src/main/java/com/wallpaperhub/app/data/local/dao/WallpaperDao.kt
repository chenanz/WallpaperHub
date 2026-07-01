package com.wallpaperhub.app.data.local.dao

import androidx.room.*
import com.wallpaperhub.app.data.local.WallpaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {

    // === 普通壁纸查询 - 永远排除 R18 ===
    @Query("SELECT * FROM wallpapers WHERE is_r18 = 0 ORDER BY created_at DESC")
    fun getNormalWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE is_r18 = 0 AND source = :source ORDER BY created_at DESC")
    fun getNormalBySource(source: String): Flow<List<WallpaperEntity>>

    // === R18 壁纸查询 - 强制 is_r18 = 1 ===
    @Query("SELECT * FROM wallpapers WHERE is_r18 = 1 ORDER BY created_at DESC")
    fun getR18Wallpapers(): Flow<List<WallpaperEntity>>

    // === 收藏查询 - 只看普通壁纸的收藏（R18收藏在隔离视图） ===
    @Query("SELECT * FROM wallpapers WHERE is_favorite = 1 AND is_r18 = 0 ORDER BY created_at DESC")
    fun getFavorites(): Flow<List<WallpaperEntity>>

    // === 单条查询 ===
    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: String): WallpaperEntity?

    // === 写入 ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallpapers: List<WallpaperEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallpaper: WallpaperEntity)

    // === 更新收藏状态 ===
    @Query("UPDATE wallpapers SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    // === 更新本地路径（离线缓存） ===
    @Query("UPDATE wallpapers SET local_path = :localPath WHERE id = :id")
    suspend fun updateLocalPath(id: String, localPath: String)

    // === 统计 ===
    @Query("SELECT COUNT(*) FROM wallpapers WHERE is_r18 = 0")
    suspend fun getNormalCount(): Int

    @Query("SELECT COUNT(*) FROM wallpapers WHERE is_r18 = 1")
    suspend fun getR18Count(): Int

    @Query("SELECT COUNT(*) FROM wallpapers WHERE is_favorite = 1")
    suspend fun getFavoriteCount(): Int

    // === 清理 ===
    @Query("DELETE FROM wallpapers WHERE is_favorite = 0 AND local_path = ''")
    suspend fun clearUnfavoritedUnCached(): Int

    @Query("DELETE FROM wallpapers")
    suspend fun clearAll()
}
