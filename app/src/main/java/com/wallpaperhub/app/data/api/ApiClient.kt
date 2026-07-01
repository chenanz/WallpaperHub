package com.wallpaperhub.app.data.api

import com.wallpaperhub.app.BuildConfig
import com.wallpaperhub.app.util.SecretUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络客户端 - 自动注入 R18 密钥
 */
object ApiClient {

    private val secretInterceptor = Interceptor { chain ->
        val request = chain.request()
        val newRequestBuilder = request.newBuilder()

        // 检查是否需要添加 Secret Key
        val requireSecret = request.header("X-Require-Secret")
        if (requireSecret == "true") {
            newRequestBuilder.removeHeader("X-Require-Secret")
            val secretKey = SecretUtils.getSecretKey()
            if (secretKey.isNotEmpty()) {
                newRequestBuilder.addHeader("X-Secret-Key", secretKey)
            }
        }

        // R18 下载接口也需要 Secret Key
        if (request.url.encodedPath.contains("/api/r18/") ||
            request.url.encodedPath.contains("/download/")) {
            val secretKey = SecretUtils.getSecretKey()
            if (secretKey.isNotEmpty() && request.header("X-Secret-Key") == null) {
                newRequestBuilder.addHeader("X-Secret-Key", secretKey)
            }
        }

        chain.proceed(newRequestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(secretInterceptor)
        .addInterceptor(Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("User-Agent", "WallpaperHub/1.0 (Android)")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: WallpaperApi = retrofit.create(WallpaperApi::class.java)
}
