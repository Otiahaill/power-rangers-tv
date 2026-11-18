# power-rangers-tv — README

**Native Android app (TV + mobile + tablet) that streams YouTube channels as a simulated live TV broadcast.**
Power Rangers theme (`#FF5200`), iOS-adapted design principles.

## Project Info
| | |
|---|---|
| Package | `com.example.prtv` |
| AGP / Kotlin | 8.5.0 / 1.9.23 |
| compileSdk / targetSdk / minSdk | 34 / 34 / 21 |
| Compose BOM | 2024.06.00 (compiler 1.5.14) |
| GitHub | https://github.com/Otiahaill/power-rangers-tv |
| Version | 1.0.0 |

## What It Does
- Enter a YouTube channel handle → resolves to a channel ID
- Browse the channel's playlists (adaptive grid: 1 column phone, 2 tablet, 4 TV)
- Play any video in an immersive YouTube player (WebView iframe, `modestbranding`)
- Simulated live broadcast: a deterministic daily schedule means every viewer sees the same video at the same time
- Live-stream detection badge when the channel is broadcasting

## Architecture
```
data/         api (Retrofit) · model (DTOs) · repository (interface + impl)
di/           NetworkModule · RepositoryModule · DataStoreModule
security/     ApiKeyManager (Keystore) · KeystoreConfig
ui/           theme · component · screen/{channelbrowser,playlistbrowser,videoplayer,settings}
viewmodel/    4 ViewModels + BroadcastScheduler
```

## API Key
The YouTube Data API v3 key is **embedded in the app** — users do not need to supply one.
It is injected at build time:

```properties
# gradle.properties.local (gitignored)
youtubeApiKey=YOUR_KEY
```

It becomes `BuildConfig.YOUTUBE_API_KEY`, is cached on first launch in `EncryptedSharedPreferences`
(Android Keystore, AES256-GCM), and can still be overridden in **Settings**.

In CI, the key is written from the `YOUTUBE_API_KEY` repository secret.

## How to Build
```sh
# Local
./gradlew :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# GitHub Actions (push to main triggers it)
# → download the "app-debug-apk" artifact from the Actions tab
```

## Known Limitations
- Playback seeking reloads the URL instead of using the YouTube IFrame JS API
- Playlist thumbnails are placeholders; Glide is wired up but not yet used
- Playlist → video navigation is not yet wired up
- The `data/`, `di/` and `res/values/` layers are missing from the current archive — the app cannot compile until they are recreated

## Documentation
- `project.md` — architecture, decisions, CI failure history
- `study_session.md` — current working state and next actions
- `changelog.md` — chronological record of every change
