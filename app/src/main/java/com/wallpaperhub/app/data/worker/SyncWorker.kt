package com.wallpaperhub.app.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wallpaperhub.app.data.local.AppDatabase
import com.wallpaperhub.app.data.repository.WallpaperRepository

/**
 * WorkManager 后台同步 - 每12小时拉取最新壁纸
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "开始后台同步壁纸...")

        return try {
            val repository = WallpaperRepository(applicationContext)
            repository.syncFromServer()
            Log.i(TAG, "后台同步完成")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "后台同步失败: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}
