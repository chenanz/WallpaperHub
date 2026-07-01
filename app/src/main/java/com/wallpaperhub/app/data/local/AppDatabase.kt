package com.wallpaperhub.app.data.local

import androidx.room.*
import com.wallpaperhub.app.data.local.dao.WallpaperDao

/**
 * Room 数据库 - 壁纸本地缓存
 * 数据隔离: 普通/R18 壁纸同表，通过 isR18 区分
 */
@Database(
    entities = [WallpaperEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wallpaperhub_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
