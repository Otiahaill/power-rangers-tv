# power-rangers-tv — Testing

## Test Framework
- **Unit tests:** JUnit 4 + kotlinx-coroutines-test (dependencies declared, no test sources written yet)
- **Instrumentation:** AndroidX Test + Espresso + Compose UI Test (dependencies declared, no tests written yet)
- **CI:** GitHub Actions `assembleDebug` only — it does not run tests

## Available Test Commands
| Command | Description |
|---|---|
| `./gradlew :app:assembleDebug` | Build the debug APK (what CI runs) |
| `./gradlew :app:testDebugUnitTest` | Unit tests (once sources exist) |
| `./gradlew :app:connectedDebugAndroidTest` | Instrumentation tests on a device/emulator |
| `./gradlew :app:lint` | Android Lint |

## Verification Performed

| Check | Method | Result |
|---|---|---|
| YouTube API key works | `curl` against `playlists` endpoint | ✅ 61 playlists returned |
| Live stream detection | `search?eventType=live&type=video` | ✅ 2 live streams returned |
| Playlist ordering | compared `playlistItems` order vs `videos.list` order | ✅ identical for the 8-item sample |
| RSS feed (keyless fallback) | `feeds/videos.xml?playlist_id=...` | ✅ 14 entries parsed |
| Handle → channel ID | `curl` the channel page, extract `externalId` | ✅ `UCqWhKZdJrvy0yHAamjWQkCA` for `@PowerRangers` (note: different from the app's default channel) |
| CI compile | GitHub Actions run `44235241650` | ❌ **failed** — `defaultConfig contains custom BuildConfig fields, but the feature is disabled` |
| CI compile (previous) | runs `44234720388`, `44234455588`, `44234190553` | ❌ failed at earlier configuration stages (see project.md § CI Build Failure History) |

**No successful compile has been achieved yet.** Every CI attempt has failed during Gradle configuration, so no Kotlin compilation or runtime verification has taken place.

## Critical User Workflows (to verify after the build succeeds)
1. **First launch** — app opens on Channel Browser; the embedded key is seeded into encrypted storage without any user input
2. **Resolve channel** — entering `@PowerRangers` (or a channel ID) resolves and stores the channel ID
3. **List playlists** — the playlist grid loads from the real API; live badge appears when the channel is live
4. **Play a video** — tapping a playlist opens the WebView player and the episode plays
5. **Simulated live** — `BroadcastScheduler.calculateBroadcastState()` returns the same video for two devices at the same moment
6. **TV adaptation** — on an expanded-width window the grid switches to 4 columns; the app appears in the Leanback launcher
7. **Settings** — the API key can be overridden and the appearance switched between Light / Dark / System

## Regression Tests
None yet — no test sources exist.

## Known Test Failures
- **CI build:** `defaultConfig contains custom BuildConfig fields, but the feature is disabled.` — fix is `buildFeatures { buildConfig = true }`, applied locally but not yet pushed.
- **On-device builds are blocked:** the harness `gradle` binary has a shebang pointing at `/data/local/tmp/androidharness/...` which the app UID cannot resolve (`bad interpreter: No such file or directory`). Compilation therefore has to happen in GitHub Actions.

## Manual Testing Checklist
- [ ] `./gradlew :app:assembleDebug` succeeds in CI
- [ ] `app-debug.apk` is uploaded as the `app-debug-apk` artifact
- [ ] APK installs with `pm install -r`
- [ ] MainActivity launches on Channel Browser
- [ ] No API key prompt blocks first launch (the key is embedded)
- [ ] Channel handle resolves; playlists load
- [ ] LIVE badge appears during a live broadcast
- [ ] Video plays in the WebView iframe player
- [ ] Two devices show the same "now on air" episode
- [ ] TV: Leanback launcher entry works; grid uses 4 columns

## Build Verification
- **Success criteria:** CI run concludes with `success`, the `app-debug-apk` artifact exists and contains `app-debug.apk`, `apksigner verify` passes, and the app launches without a crash in `read_logcat`.
- **Current state:** NOT VERIFIED — the build has never passed configuration.
