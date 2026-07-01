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
@TypeConverters(Converters::class)
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

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey val id: Int,
    val url: String,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String? = null,
    val source: String,
    val tags: List<String> = emptyList(),
    val resolution: String? = null,
    @ColumnInfo(name = "is_r18") val isR18: Boolean = false,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "local_path") val localPath: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: String? = null
)

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.joinToString(",")

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.split(",")?.map { it.trim() } ?: emptyList()
}
