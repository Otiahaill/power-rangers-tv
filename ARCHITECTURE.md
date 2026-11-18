# power-rangers-tv — Architecture

## System Components

| Component | Responsibility |
|---|---|
| `MainActivity` | `@AndroidEntryPoint`, hosts the Compose `NavHost` with 4 routes: `channel_browser`, `playlist_browser`, `video_player`, `settings` |
| `PowerRangersTVApp` | `@HiltAndroidApp`; seeds the API key into encrypted storage on first launch |
| `data/api/YouTubeApiService` | Retrofit interface over YouTube Data API v3 — `playlists`, `playlistItems`, `search`, `channels`, `videos` |
| `data/model/YouTubeModels` | Gson DTOs (`YouTubePlaylist`, `YouTubeVideo`, response wrappers) + `parseDuration()` for ISO-8601 `PT1H2M3S` |
| `data/repository/YouTubeRepository` | Interface + `Result` sealed class (`Loading` / `Success` / `Error`) |
| `data/repository/YouTubeRepositoryImpl` | Flow-based implementation; `ApiKeyProvider` interface lives here |
| `di/NetworkModule` | Provides `OkHttpClient`, `Gson`, `Retrofit`, `YouTubeApiService` |
| `di/RepositoryModule` | Binds `YouTubeRepositoryImpl` → `YouTubeRepository` |
| `di/DataStoreModule` | RxDataStore-backed preference flows for API key, channel ID, handle, theme mode |
| `security/ApiKeyManager` | Reads the encrypted key first, falls back to the embedded `BuildConfig` key |
| `security/KeystoreConfig` | AES256-GCM master-key generation, hardware-backed detection |
| `ui/theme/PowerRangersTheme` | PR red `#FF5200`; iOS-style light/dark colour schemes, typography scale, shapes |
| `ui/screen/channelbrowser` | Handle entry, resolves to a channel ID, navigates on success |
| `ui/screen/playlistbrowser` | Adaptive playlist grid (1/2/4 columns) + LIVE badge |
| `ui/screen/videoplayer` | WebView YouTube iframe player, transport controls, Up Next |
| `ui/screen/settings` | API key override, appearance switches, about |
| `ui/component/PlaylistCard` | 16:9 thumbnail + title + video count |
| `viewmodel/BroadcastScheduler` | Deterministic daily schedule (seeded shuffle + UTC epoch) |
| `viewmodel/*ViewModel` | Hilt ViewModels exposing UI state as Compose `StateFlow` |

## Data Flow

```
User types handle
      │
      ▼
ChannelBrowserScreen ──► ChannelBrowserViewModel.loadChannel()
      │                            │
      │                            ▼
      │                   YouTubeRepository.resolveChannelId()
      │                            │  Retrofit → channels?forHandle=
      │                            ▼
      │                   channelId ──► saved to DataStore
      │                            │
      ▼                            ▼
navController.navigate("playlist_browser")
                                   │
                                   ▼
                    PlaylistBrowserViewModel.loadPlaylists(channelId)
                                   │
                    ┌──────────────┴──────────────┐
                    ▼                             ▼
        repository.getPlaylists()      repository.getLiveStream()
                    │                             │
                    ▼                             ▼
            LazyVerticalGrid                  LIVE badge
              (1/2/4 cols)
```

## Broadcast Scheduling (the "live channel" behaviour)
1. `seededShuffle(videos, daySeed)` — LCG shuffle, `daySeed = YYYYMMDD` (UTC). Every device produces the same order.
2. `totalDuration` = sum of all episode durations.
3. `position = (now − epoch) % totalDuration` where epoch = 2024-01-01 00:00:00 UTC.
4. Walk the shuffled list accumulating durations until the position falls inside an episode.
5. `BroadcastState` carries the video id, index and the offset into that episode.

The result: the app behaves like a TV channel — whatever is "on air" is the same for everyone, and it loops.

## External Services
- **YouTube Data API v3** — playlists, playlist items, search (live), channels, video metadata
- **youtube-nocookie.com** — iframe embed used by the WebView player

## Security Boundaries
- API key never in version control: `gradle.properties.local` (gitignored) → `BuildConfig` → `EncryptedSharedPreferences`
- Android Keystore holds the master key (AES256-GCM); hardware-backed when the device supports it
- Release builds run R8 (`isMinifyEnabled = true`) so the key string is obfuscated in the APK
- No user accounts, no personal data collected

## Adaptive / TV Strategy
| Form factor | Trigger | Layout |
|---|---|---|
| Phone | `WindowWidthSizeClass.Compact` | 1-column grid, stacked cards |
| Tablet | `WindowWidthSizeClass.Medium` | 2-column grid |
| TV | `WindowWidthSizeClass.Expanded` | 4-column grid, Leanback launcher entry (`android.intent.category.LEANBACK_LAUNCHER`) |

## Known Limitations
- Playback seeking reloads the URL rather than calling the IFrame JS API
- Glide is a dependency but thumbnails are still placeholders
- Playlist → video navigation is a no-op placeholder
- PlaylistBrowser does not auto-load the stored channel ID yet
- The `data/`, `di/` and `res/values/` layers are missing from the current archive, so the app cannot compile as archived

## Architecture Decision Records
- **ADR 001** — Compose over XML views: single language, faster iteration, matches the modern Android baseline
- **ADR 002** — Hilt over manual DI: compile-time graph, scales with the repository/data-store split
- **ADR 003** — Retrofit + Gson over raw `HttpURLConnection`: typed API surface, interceptors, less boilerplate
- **ADR 004** — Embedded API key over per-user keys: the user explicitly rejected the "bring your own key" flow; the key is obfuscated by R8 and cached in the Keystore
- **ADR 005** — WebView iframe over the YouTube Player SDK: no dependency on the YouTube app being installed, works identically on TV and handset, and matches the original web app's player
- **ADR 006** — UTC epoch + YYYYMMDD seed for the schedule: guarantees the same programme for every user regardless of local timezone
