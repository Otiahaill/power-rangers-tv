# ProGuard rules for Power Rangers TV

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class * extends dagger.hilt.android.HiltViewModel

# Keep Room entities and DAOs
-keep class com.example.prtv.data.model.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Retrofit/Gson models
-keep class com.example.prtv.data.model.** { *; }

# Keep Compose view models
-keep class com.example.prtv.viewmodel.** { *; }

# Keep WebView JavaScript interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Glide
-keep class com.bumptech.glide.** { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Retrofit/OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }

# Keep Kotlin stdlib
-keep class kotlin.** { *; }

# Keep AndroidX libraries
-keep class androidx.** { *; }
-keep class com.google.android.** { *; }
