package com.wallpaperhub.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 壁纸数据模型 - API 响应
 */
data class WallpaperResponse(
    val wallpapers: List<WallpaperDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val pages: Int? = null
)

data class WallpaperDto(
    val id: Int,
    val url: String,
    val source: String,
    val tags: List<String>? = emptyList(),
    val resolution: String? = null,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("is_r18")
    val isR18: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String? = null
)

/**
 * 壁纸统计
 */
data class StatsResponse(
    @SerializedName("normal_count")
    val normalCount: Int,
    @SerializedName("r18_count")
    val r18Count: Int,
    @SerializedName("total_count")
    val totalCount: Int,
    val sources: List<SourceStats>
)

data class SourceStats(
    val name: String,
    val count: Int
)
