package com.wallpaperhub.app.data.api

import com.wallpaperhub.app.data.model.WallpaperResponse
import com.wallpaperhub.app.data.model.StatsResponse
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * Retrofit API 接口
 * R18 接口通过 Interceptor 自动添加 X-Secret-Key header
 */
interface WallpaperApi {

    @GET("/api/wallpapers")
    suspend fun getNormalWallpapers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String = "normal",
        @Query("tags") tags: String? = null,
        @Query("sort") sort: String = "newest"
    ): WallpaperResponse

    @GET("/api/r18/wallpapers")
    @Headers("X-Require-Secret: true")
    suspend fun getR18Wallpapers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): WallpaperResponse

    @GET("/api/download/{fileId}")
    @Streaming
    suspend fun downloadWallpaper(
        @Path("fileId") fileId: Int,
        @HeaderMap headers: Map<String, String> = emptyMap()
    ): ResponseBody

    @GET("/api/stats")
    suspend fun getStats(): StatsResponse

    @GET("/api/health")
    suspend fun healthCheck(): Map<String, String>
}
