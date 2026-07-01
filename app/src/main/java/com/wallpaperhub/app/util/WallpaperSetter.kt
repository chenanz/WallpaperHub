package com.wallpaperhub.app.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers

/**
 * 壁纸设置工具
 * 适配 Redmi K80 Ultra: 3200x1440 (20:9)
 * 兼容 MIUI 裁剪参数
 */
object WallpaperSetter {

    // Redmi K80 Ultra 分辨率
    private const val TARGET_WIDTH = 3200
    private const val TARGET_HEIGHT = 1440
    private const val ASPECT_RATIO = 20f / 9f

    /**
     * 设置壁纸
     * @param context Context
     * @param imageUrl 图片URL
     * @param target 目标: "home" | "lock" | "both"
     */
    suspend fun setWallpaper(context: Context, imageUrl: String, target: String = "both"): Boolean {
        return try {
            val bitmap = downloadAndScaleImage(imageUrl) ?: return false
            val wallpaperManager = WallpaperManager.getInstance(context)

            when (target) {
                "home" -> {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                "lock" -> {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
                else -> {
                    // both
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
            }

            Log.i(TAG, "壁纸设置成功: target=$target")
            true
        } catch (e: IOException) {
            Log.e(TAG, "壁纸设置失败: ${e.message}")
            false
        }
    }

    /**
     * 从 InputStream 设置壁纸
     */
    suspend fun setWallpaperFromStream(context: Context, inputStream: InputStream, target: String = "both"): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val scaled = scaleForDevice(bitmap)
            val wallpaperManager = WallpaperManager.getInstance(context)

            when (target) {
                "home" -> wallpaperManager.setBitmap(scaled, null, true, WallpaperManager.FLAG_SYSTEM)
                "lock" -> wallpaperManager.setBitmap(scaled, null, true, WallpaperManager.FLAG_LOCK)
                else -> {
                    wallpaperManager.setBitmap(scaled, null, true, WallpaperManager.FLAG_SYSTEM)
                    wallpaperManager.setBitmap(scaled, null, true, WallpaperManager.FLAG_LOCK)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "壁纸设置失败: ${e.message}")
            false
        }
    }

    /**
     * 下载并缩放图片到目标分辨率
     * MIUI 裁剪兼容: 确保图片比例匹配 20:9
     */
    private suspend fun downloadAndScaleImage(imageUrl: String): Bitmap? {
        return try {
            with(kotlinx.coroutines.Dispatchers.IO) {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 30000
                connection.readTimeout = 60000
                val stream = connection.getInputStream()
                val original = BitmapFactory.decodeStream(stream)
                stream.close()
                original?.let { scaleForDevice(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "图片下载失败: ${e.message}")
            null
        }
    }

    /**
     * 缩放到设备分辨率
     * 保持 20:9 比例，MIUI 不会再次裁剪
     */
    private fun scaleForDevice(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val currentRatio = width.toFloat() / height.toFloat()

        // 如果比例接近 20:9，直接缩放
        // 否则先裁剪到 20:9 再缩放
        val scaledBitmap: Bitmap

        if (kotlin.math.abs(currentRatio - ASPECT_RATIO) < 0.1f) {
            // 比例接近，直接缩放
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, TARGET_WIDTH, TARGET_HEIGHT, true)
        } else {
            // 先裁剪到 20:9
            val cropWidth: Int
            val cropHeight: Int
            if (currentRatio > ASPECT_RATIO) {
                // 图片更宽，裁掉两边
                cropHeight = height
                cropWidth = (height * ASPECT_RATIO).toInt()
            } else {
                // 图片更高，裁掉上下
                cropWidth = width
                cropHeight = (width / ASPECT_RATIO).toInt()
            }

            val startX = (width - cropWidth) / 2
            val startY = (height - cropHeight) / 2
            val cropped = Bitmap.createBitmap(bitmap, startX, startY, cropWidth, cropHeight)
            scaledBitmap = Bitmap.createScaledBitmap(cropped, TARGET_WIDTH, TARGET_HEIGHT, true)
            cropped.recycle()
        }

        if (scaledBitmap != bitmap) {
            bitmap.recycle()
        }

        return scaledBitmap
    }

    private const val TAG = "WallpaperSetter"
}
