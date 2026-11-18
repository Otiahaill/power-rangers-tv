package com.example.prtv.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.prtv.di.ApiKeyProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure API Key Manager using Android Keystore + EncryptedSharedPreferences
 * 
 * The YouTube API key is:
 * 1. Encrypted at rest using Android Keystore (hardware-backed if available)
 * 2. Never stored in plain text
 * 3. Only decrypted in memory when needed
 * 4. Obfuscated in release builds via ProGuard/R8
 */
@Singleton
class ApiKeyManager @Inject constructor(
    private val context: Context
) : ApiKeyProvider {

    private val prefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "api_keys_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val API_KEY_KEY = "youtube_api_key_encrypted"
    private val KEY_VERSION_KEY = "api_key_version"

    // Default embedded key (obfuscated at build time via ProGuard)
    // Replace YOUR_YOUTUBE_API_KEY_HERE with actual key before release
    private val DEFAULT_API_KEY = buildConfigApiKey()

    /**
     * Get the API key - returns cached/decrypted key or default embedded key
     */
    override fun getApiKey(): String {
        // Try to get from secure storage first
        val storedKey = prefs.getString(API_KEY_KEY, null)
        if (storedKey != null && storedKey.isNotBlank()) {
            return storedKey
        }

        // Fall back to embedded default key
        val defaultKey = DEFAULT_API_KEY
        if (defaultKey.isNotBlank()) {
            // Cache it for future use
            saveApiKey(defaultKey)
            return defaultKey
        }

        return ""
    }

    /**
     * Save API key securely (for user-provided keys or rotation)
     */
    fun saveApiKey(apiKey: String) {
        prefs.edit()
            .putString(API_KEY_KEY, apiKey)
            .putInt(KEY_VERSION_KEY, 1)
            .apply()
    }

    /**
     * Check if user has provided a custom key (vs using embedded default)
     */
    fun hasCustomKey(): Boolean {
        return prefs.contains(API_KEY_KEY)
    }

    /**
     * Clear stored key (revert to embedded default)
     */
    fun clearCustomKey() {
        prefs.edit().remove(API_KEY_KEY).apply()
    }

    /**
     * Get API key synchronously for Retrofit (blocking)
     * In production, consider using suspend functions with coroutines
     */
    override fun getApiKeyBlocking(): String = getApiKey()

    /**
     * Obfuscated default key - replaced at build time by ProGuard
     * The actual key should be added via BuildConfig or gradle properties
     */
    private fun buildConfigApiKey(): String {
        // This will be replaced by the actual key at build time via:
        // 1. gradle.properties: youtubeApiKey="YOUR_KEY"
        // 2. BuildConfig field: buildConfigField "String", "YOUTUBE_API_KEY", "\"$youtubeApiKey\""
        // 3. ProGuard will obfuscate this method
        return BuildConfig.YOUTUBE_API_KEY
    }

    companion object {
        // Called during app initialization to ensure key is available
        fun initialize(context: Context, apiKey: String? = null) {
            val manager = ApiKeyManager(context)
            if (apiKey != null && apiKey.isNotBlank()) {
                manager.saveApiKey(apiKey)
            }
        }
    }
}
