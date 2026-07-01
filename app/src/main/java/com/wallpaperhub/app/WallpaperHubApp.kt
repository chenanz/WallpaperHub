package com.wallpaperhub.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.wallpaperhub.app.data.local.AppDatabase
import com.wallpaperhub.app.data.worker.SyncWorker
import com.wallpaperhub.app.util.SecretUtils
import androidx.work.*

class WallpaperHubApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // 初始化加密存储
        SecretUtils.init(this)
        // 初始化数据库
        AppDatabase.getInstance(this)
        // 启动后台同步
        scheduleSyncWork()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .crossfade(300)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }

    private fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(12, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, java.util.concurrent.TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "wallpaper_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
