package com.wallpaperhub.app.data.local

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
        Index(value = ["isR18"]),
        Index(value = ["isFavorite"])
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
    val isR18: Boolean = false,
    val isFavorite: Boolean = false,
    val category: String = "normal",  // normal, live, 3d
    val localPath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
