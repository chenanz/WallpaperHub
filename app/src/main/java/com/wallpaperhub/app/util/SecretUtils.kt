package com.wallpaperhub.app.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 加密工具 - Android KeyStore 存储
 * - 隐藏入口密码
 * - API Secret Key
 * - 所有敏感信息加密存储
 */
object SecretUtils {

    private const val KEY_ALIAS = "wallpaperhub_master_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PREFS_FILE = "wallpaperhub_secret_prefs"

    private lateinit var appContext: Context
    private lateinit var masterKey: MasterKey
    private lateinit var encryptedPrefs: android.content.SharedPreferences

    // 默认暗号密码
    private const val DEFAULT_PASSWORD = "5201314"

    fun init(context: Context) {
        appContext = context.applicationContext
        masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedPrefs = EncryptedSharedPreferences.create(
            appContext,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // === 密码验证 ===

    fun verifyPassword(input: String): Boolean {
        val storedPassword = encryptedPrefs.getString("secret_password", DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
        return input == storedPassword
    }

    fun setPassword(password: String) {
        encryptedPrefs.edit().putString("secret_password", password).apply()
    }

    fun getPassword(): String {
        return encryptedPrefs.getString("secret_password", DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    // === API Secret Key ===

    fun getSecretKey(): String {
        return encryptedPrefs.getString("api_secret_key", "") ?: ""
    }

    fun setSecretKey(key: String) {
        encryptedPrefs.edit().putString("api_secret_key", key).apply()
    }

    // === 隐藏入口标记（加密存储） ===

    private var _unlocked = false
    val isUnlocked: Boolean get() = _unlocked

    fun unlock() {
        _unlocked = true
        // 使用 KeyStore 加密标记入口已解锁
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias("unlock_marker")) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
                )
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        "unlock_marker",
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(true)
                        .build()
                )
                keyGenerator.generateKey()
            }
            encryptedPrefs.edit().putBoolean("entry_unlocked", true).apply()
        } catch (e: Exception) {
            // 即使 KeyStore 失败也允许进入（内存标记）
        }
    }

    fun lock() {
        _unlocked = false
        encryptedPrefs.edit().putBoolean("entry_unlocked", false).apply()
    }

    // === 额外加密（用于最高安全性场景） ===

    private fun getOrCreateAesKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateAesKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(ciphertext: String): String {
        val combined = Base64.decode(ciphertext, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateAesKey(), GCMParameterSpec(128, iv))
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }
}
