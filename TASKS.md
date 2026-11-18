# power-rangers-tv — Tasks

## Critical Defects
- [ ] **Restore the missing `data/`, `di/` and `res/values/` layers** — the app cannot compile without them. (User said not to add them to the zip; they must still be recreated in the project for a build to succeed.)
- [ ] **Push the `buildConfig = true` fix** — locally applied, not yet committed. CI failure #10: `defaultConfig contains custom BuildConfig fields, but the feature is disabled.`
- [ ] **Verify `gradle.properties.local` is actually read** — `project.findProperty("youtubeApiKey")` may not pick up a `.local` file. Either include it from `gradle.properties` or pass `-PyoutubeApiKey=...`.

## Security
- [ ] Create the `YOUTUBE_API_KEY` GitHub Actions repository secret (the CI workflow references it but it may not exist)
- [ ] Confirm `gradle.properties.local` and `local.properties` are ignored by git
- [ ] Restore `app/proguard-rules.pro` (needed for release builds; R8 keeps are missing)
- [ ] Confirm release builds obfuscate the key (R8 `isMinifyEnabled = true` is set, but the rules file is absent)

## Core Functionality
- [x] Project skeleton, package `com.example.prtv`, layered architecture
- [x] Power Rangers theme with iOS-adapted light/dark schemes, typography and shapes
- [x] 4 screens: Channel Browser, Playlist Browser, Video Player, Settings
- [x] 4 ViewModels with Compose `StateFlow` state
- [x] `BroadcastScheduler` — seeded shuffle + UTC epoch schedule
- [x] Embedded + Keystore-protected API key flow
- [x] Leanback launcher intent filter for Android TV
- [x] Adaptive grid via `WindowWidthSizeClass` (1 / 2 / 4 columns)
- [x] CI workflow writing the key from a secret and uploading `app-debug-apk`
- [ ] Playlist card click → `video_player` route
- [ ] PlaylistBrowser auto-loads the stored channel ID on entry
- [ ] Real thumbnails via Glide
- [ ] Live badge click → play the live stream
- [ ] Seek bar wired to the IFrame JS API
- [ ] Watch progress persisted per playlist

## Improvements
- [ ] Restore the `values/`, `drawable/splash_background.xml` and `mipmap-anydpi-v26/` resources
- [ ] Add a splash screen
- [ ] Handle API quota errors with a friendly message
- [ ] Retry/backoff on network failure
- [ ] Add a "now on air" card using `BroadcastScheduler`

## Technical Debt
- [ ] `gradlew` is a hand-written shim, not the official Gradle wrapper jar — replace with the real wrapper (`gradle wrapper`) once a build can run
- [ ] `apiKeyProvider.getApiKey()` is synchronous inside a Flow — move to a suspend function
- [ ] `ApiKeyManager` declares `getApiKeyBlocking()` that is not on the `ApiKeyProvider` interface
- [ ] `ApiKeyManager` imports `com.example.prtv.di.ApiKeyProvider` but that interface is declared in the repository package
- [ ] `ApiKeyManager` references `BuildConfig` without importing `com.example.prtv.BuildConfig`
- [ ] `ChannelBrowserViewModel` imports `YouTubeRepositoryImpl` and `ActivityRetainedScoped` but does not use them
- [ ] `MainActivity` imports `EntryPoint`, `HiltAndroidApp`, `Inject`, `HiltViewModel` unnecessarily
- [ ] `power-rangers-tw` GitHub repo still exists but the directory was deleted — archive or delete the repo

## Future Development
- [ ] Real-time live stream overriding the simulated schedule
- [ ] Room database for offline catalogue caching
- [ ] Download for offline viewing
- [ ] Chromecast support
- [ ] Release signing + AAB for Play Store
- [ ] Google Play TV (Leanback) certification assets

## Acceptance Criteria
- [x] All 7 mandatory memory files exist and match disk
- [x] API key is embedded — no user-supplied key required
- [ ] CI compiles successfully and publishes `app-debug-apk`
- [ ] APK installs and launches on a handset
- [ ] Channel handle resolves; playlists load from the real API
- [ ] Video plays in the WebView player
- [ ] Broadcast schedule shows the same programme for two devices
- [ ] TV launcher entry appears and the layout uses 4 columns
