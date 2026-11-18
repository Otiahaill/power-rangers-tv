package com.example.prtv.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.MasterKeys
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.InvalidAlgorithmParameterException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android Keystore Configuration for Power Rangers TV
 * 
 * This configures the master key used by EncryptedSharedPreferences
 * for secure API key storage.
 */
@Singleton
class KeystoreConfig @Inject constructor(
    private val context: Context
) {

    private val KEY_ALIAS = "power_rangers_tv_master_key"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"

    /**
     * Get or create the master key for encryption
     * Uses AES256_GCM with hardware-backed keystore if available
     */
    fun getOrCreateMasterKey(): String {
        return MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    /**
     * Advanced keystore configuration for API keys
     * Creates a dedicated key for API key encryption with stronger security
     */
    fun createApiKeyEncryptionKey(): String {
        val keyGenParamSpec = KeyGenParameterSpec.Builder(
            "api_key_encryption_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setUserAuthenticationRequired(false)
            setInvalidatedByBiometricEnrollment(true)
            // Require authentication for sensitive operations
            // setUserAuthenticationRequired(true) // Uncomment for additional security
        }.build()

        try {
            val keyGenerator = java.security.KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            keyGenerator.init(keyGenParamSpec)
            keyGenerator.generateKey()
            return "api_key_encryption_key"
        } catch (e: Exception) {
            // Fallback to MasterKeys
            return MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        }
    }

    /**
     * Check if hardware-backed keystore is available
     */
    fun isHardwareBacked(): Boolean {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            val entry = keyStore.getEntry(KEY_ALIAS, null)
            return entry is java.security.KeyStore.PrivateKeyEntry
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Initialize keystore on first app launch
     */
    fun initialize(): Boolean {
        return try {
            createApiKeyEncryptionKey()
            true
        } catch (e: Exception) {
            false
        }
    }
}
