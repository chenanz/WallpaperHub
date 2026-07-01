package com.wallpaperhub.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room 实体 - 壁纸本地缓存
 * 普通/R18 通过 isR18 字段区分
 */
@Entity(
    tableName = "wallpapers",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["is_r18"]),
        Index(value = ["is_favorite"])
    ]
)
data class WallpaperEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val thumbnailUrl: String = "",
    val source: String = "",
    val tags: String = "",  // JSON array string
    val resolution: String = "",
    @ColumnInfo(name = "is_r18")
    val isR18: Boolean = false,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    val category: String = "normal",  // normal, live, 3d
    @ColumnInfo(name = "local_path")
    val localPath: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
