# WallpaperHub ProGuard Rules
# 保护隐藏入口和 API 密钥不被逆向

# ============ 通用 Android 规则 ============

# 保留 Application 子类
-keep class com.wallpaperhub.app.WallpaperHubApp { *; }

# 保留 MainActivity
-keep class com.wallpaperhub.app.MainActivity { *; }

# ============ Room 数据库 ============

-keep class com.wallpaperhub.app.data.local.WallpaperEntity { *; }
-keep class com.wallpaperhub.app.data.local.Converters { *; }
-keep class * extends androidx.room.RoomDatabase

# ============ Retrofit ============

-keepattributes Signature
-keepattributes Exceptions

-keep class com.wallpaperhub.app.data.model.** { *; }
-keep interface com.wallpaperhub.app.data.api.WallpaperApi { *; }
-keepclassmembers interface com.wallpaperhub.app.data.api.WallpaperApi {
    <methods>;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============ Coil 图片加载 ============

-dontwarn coil.**
-keep class coil.** { *; }

# ============ 隐藏模块保护 ============

# 隐藏入口类 - 类名混淆为无意义字母
# 原始类名: aaa (已在代码中定义) - ProGuard 会进一步混淆
-keepclassmembers class com.wallpaperhub.app.ui.screens.aaa {
    *** onLongClick(android.view.View);
}

# R18 路由常量 - 混淆 Base64 字符串
# 不保留 Routes.R18_GALLERY 的字段名
-keepclassmembers class com.wallpaperhub.app.ui.navigation.Routes {
    *** R18_BASE;
}

# 密码验证方法 - 保留逻辑但混淆类名
-keepclassmembers class com.wallpaperhub.app.util.SecretUtils {
    *** verifyPassword(java.lang.String);
    *** getSecretKey();
    *** setSecretKey(java.lang.String);
    *** unlock();
    *** lock();
}

# 截屏防护类
-keepclassmembers class com.wallpaperhub.app.util.ScreenshotBlocker {
    *** enable(android.view.Window);
    *** disable(android.view.Window);
}

# ============ 积极混淆 ============

# 混淆所有内部类名（除了上面保留的）
-repackageclasses ''
-allowaccessmodification

# 字符串加密 - 移除调试信息
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ============ Compose 相关 ============

-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ============ WorkManager ============

-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keepclassmembers class com.wallpaperhub.app.data.worker.SyncWorker { *; }

# ============ 加密库 ============

-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-dontwarn javax.crypto.**
-dontwarn java.security.**

# ============ DataStore ============

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
