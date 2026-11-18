package com.example.prtv

import android.app.Application
import com.example.prtv.security.ApiKeyManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PowerRangersTVApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize API Key Manager with embedded key from BuildConfig
        // The key is embedded in the APK via buildConfigField in app/build.gradle.kts
        // and is obfuscated by ProGuard/R8 in release builds
        ApiKeyManager.initialize(this, BuildConfig.YOUTUBE_API_KEY)
    }
}
