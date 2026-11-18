# power-rangers-tv — Project Memory

**Last updated:** 2026-11-18 — after full project restart (scrapped and rebuilt from zero)

## Project Identity
- **Name:** power-rangers-tv
- **Package:** `com.example.prtv`
- **Purpose:** Native Android app (TV + mobile + tablet) that streams YouTube channels as a simulated live TV broadcast. Power Rangers theme, iOS-adapted design principles.
- **Primary Entry Point:** `app/src/main/java/com/example/prtv/MainActivity.kt`
- **GitHub:** https://github.com/Otiahaill/power-rangers-tv (public, main)
- **Status:** ACTIVE — CI build being debugged; last known CI failure was AGP/Gradle incompatibility, not app code

## History (important — do not repeat old mistakes)
1. **2026-11-17 — First attempt (SCRAPPED).** Package `com.example.tvapp`. UI files existed but were non-compiling: illegal Compose imports (`androidx.compose.ui.layout.Column`, `textFieldVorigin`, `rememberClickable`), `extendTypography` without Material3, `@ContentView` annotation, `WatchProgressHolder` using `remember` outside a composable, missing `app/build.gradle.kts`, `include ":app"` syntax error, `apply true` instead of `apply false`, missing `gradlew`, Android-only gradlew shebang breaking CI. **All of this was deleted.**
2. **2026-11-18 — Full restart.** Both `power-rangers-tv` and `power-rangers-tw` were deleted. Only `power-rangers-tv` was rebuilt from zero with a proper layered architecture. Package changed to `com.example.prtv`.

## Technology Stack (current, verified in build.gradle.kts)
- **AGP:** 8.5.0 (via `buildscript` classpath — NOT `plugins {}` block, because AGP is not on the Gradle Plugin Portal)
- **Kotlin:** 1.9.23
- **Compose compiler extension:** 1.5.14
- **Compose BOM:** 2024.06.00
- **compileSdk/targetSdk:** 34, **minSdk:** 21
- **DI:** Hilt 2.48
- **Network:** Retrofit 2.9.0 + Gson converter + OkHttp logging interceptor
- **Storage:** DataStore Preferences (RxJava3 flavour), security-crypto 1.1.0-alpha06, Room 2.6.1
- **Images:** Glide 4.16.0, **WebView:** androidx.webkit 1.8.0

## Architecture (as built)
```
com.example.prtv
├── MainActivity.kt              @AndroidEntryPoint, NavHost with 4 routes
├── PowerRangersTVApp.kt         @HiltAndroidApp, initialises ApiKeyManager with BuildConfig key
├── data/
│   ├── api/YouTubeApiService.kt        Retrofit interface (playlists, playlistItems, search, channels, videos)
│   ├── model/YouTubeModels.kt          DTOs + parseDuration()
│   └── repository/
│       ├── YouTubeRepository.kt        interface + Result sealed class
│       └── YouTubeRepositoryImpl.kt    implementation + ApiKeyProvider interface
├── di/                            NetworkModule, RepositoryModule, DataStoreModule
├── security/
│   ├── ApiKeyManager.kt          Keystore + EncryptedSharedPreferences, embedded BuildConfig fallback
│   └── KeystoreConfig.kt         AES256_GCM key generation, hardware-backed detection
├── ui/
│   ├── theme/PowerRangersTheme.kt  PR red #FF5200, iOS light/dark schemes, Typography, Shapes
│   ├── component/PlaylistCard.kt
│   └── screen/
│       ├── channelbrowser/ChannelBrowserScreen.kt
│       ├── playlistbrowser/PlaylistBrowserScreen.kt
│       ├── videoplayer/VideoPlayerScreen.kt    WebView YouTube iframe player
│       └── settings/SettingsScreen.kt
└── viewmodel/
    ├── BroadcastScheduler.kt     seededShuffle + calculateBroadcastState (ported from web app)
    ├── ChannelBrowserViewModel.kt, PlaylistBrowserViewModel.kt,
    ├── VideoPlayerViewModel.kt, SettingsViewModel.kt
```

## API Key Strategy (decided by user — do not revert)
User rejected the "every user brings their own key" workflow. The key is **embedded in the app**:
- Key lives in `gradle.properties.local` (gitignored) → `buildConfigField("String", "YOUTUBE_API_KEY", ...)` → `BuildConfig.YOUTUBE_API_KEY`
- `ApiKeyManager` seeds `EncryptedSharedPreferences` (Android Keystore, AES256-GCM) at first launch
- Users can still override it in Settings; embedded key is the default so the app works out of the box
- CI injects the key via GitHub Actions secret `YOUTUBE_API_KEY` into `gradle.properties.local`
- **User-provided key:** `AIzaSyDuKdQBwux7m8LC2PzOIjpZzwZZbhkdUDA` (stored in `gradle.properties.local`, never committed)

## Critical Workflows (must not be casually changed)
1. **Broadcast scheduling** — `BroadcastScheduler.calculateBroadcastState()` uses a seeded LCG shuffle (seed = YYYYMMDD) plus a UTC epoch. Changing the seed or epoch breaks "everyone sees the same video at the same time".
2. **API key resolution** — `ApiKeyManager.getApiKey()` checks encrypted storage first, then the embedded `BuildConfig` key. Do not remove the embedded fallback.
3. **Room/DAO schema** — entities and DAOs are interdependent; changes need migrations.
4. **Android Keystore** — the master key alias and pref names must stay stable or stored keys become unreadable.

## Known Limitations
- Playback uses a YouTube iframe in a WebView; seeking is done by reloading the URL rather than via the IFrame JS API.
- Playlist thumbnails are placeholders; Glide is wired up but not yet used for real images.
- Playlist→video navigation is not yet wired (card click is a no-op placeholder).
- PlaylistBrowser does not yet auto-load the stored channel ID on entry.
- `gradlew` is a hand-written shim, not the official Gradle wrapper jar; it resolves `gradle` from `GRADLE_HOME` or `PATH`.

## CI Build Failure History (chronological — each was fixed)
1. `chmod: Operation not permitted` on `$ANDROID_HOME` → removed the chmod step
2. `actions/setup-android` repository not found → removed; used the runner's preinstalled SDK at `/usr/local/lib/android/sdk`
3. `./gradlew: No such file or directory` → copied the wrapper in
4. `include ":app"` — Function invocation expected → `include(":app")`
5. `Plugin [id: 'com.android.application', version: '8.5.0'] was not found` → moved AGP to `buildscript` classpath
6. `./gradlew: /system/bin/sh: bad interpreter` → changed shebang from `#!/system/bin/sh` to `#!/bin/bash`
7. `task clean(type: Delete)` syntax error under Gradle 9.x → `tasks.register("clean", Delete::class)`
8. `buildConfigField(...)` string interpolation syntax error → extracted `val youtubeApiKey` first
9. `shrinkResources` unresolved → removed (not available on this AGP/Gradle combo)
10. **`defaultConfig contains custom BuildConfig fields, but the feature is disabled`** → add `buildConfig = true` to `buildFeatures` **(current fix, pushed as commit `b75e202` + follow-up)**

## Current State
- **On disk:** 17 Kotlin files, 1 AndroidManifest, 5 launcher PNGs, gradle config, CI workflow. **The `data/`, `di/`, `res/values/`, `res/drawable/splash_background.xml`, `res/mipmap-anydpi-v26/`, `app/proguard-rules.pro` files are NOT present on disk** — they were deleted during the restart and the user chose NOT to restore them. The zip therefore reflects the disk state exactly.
- **Last commit pushed:** `b75e202` (fix: remove shrinkResources) — followed by local uncommitted edits adding `buildConfig = true` and `packaging { }` (Gradle 9 renamed `packagingOptions`).
- **Next action:** push the `buildConfig = true` fix, watch CI, iterate on the next error.
