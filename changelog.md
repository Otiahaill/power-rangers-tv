# power-rangers-tv — Changelog

All entries are chronological. Status descriptions are accurate — nothing is claimed as "complete" unless a tool result proved it.

## 2026-11-18 — UHOS Project Memory Rebuilt for Restarted Project
- **Feature:** `project.md`, `study_session.md`, `changelog.md` rewritten to describe the current `com.example.prtv` codebase
- **Why:** the previous memory described the scrapped `com.example.tvapp` project; memory must always match disk
- **Status:** Completed — memory layer now matches the on-disk state
- **Reference:** this session

## 2026-11-18 — Project Fully Restarted (Previous Implementation Scrapped)
- **Removed:** entire `power-rangers-tv` and `power-rangers-tw` directories
- **Reason:** the earlier code was structurally broken (illegal Compose imports, `extendTypography` without Material3, `@ContentView`, `remember` used outside a composable, missing `app/build.gradle.kts`, `include ":app"` syntax error, `apply true` instead of `apply false`, missing `gradlew`, Android-only gradlew shebang)
- **Added:** fresh `power-rangers-tv` with package `com.example.prtv` and a layered architecture (data / di / security / ui / viewmodel)
- **Status:** Completed — new skeleton in place
- **Reference:** commits `8463551` … `e6a49ce`

## 2026-11-18 — Embedded + Keystore-Protected API Key (User Requirement)
- **Feature:** `security/ApiKeyManager.kt` and `security/KeystoreConfig.kt`; key embedded via `BuildConfig.YOUTUBE_API_KEY` and cached in `EncryptedSharedPreferences` (Android Keystore, AES256-GCM)
- **Why:** user rejected the workflow where every app user must supply their own YouTube API key
- **Files:** `app/build.gradle.kts` (buildConfigField), `gradle.properties.local` (gitignored, holds the key), `.github/workflows/ci.yml` (injects the `YOUTUBE_API_KEY` Actions secret)
- **Status:** Completed — implementation written; not yet compile-verified
- **Reference:** commits `0ee011a`, `e6a49ce`

## 2026-11-18 — CI Workflow Rewritten for Route A
- **Feature:** `.github/workflows/ci.yml` — JDK 17, preinstalled runner SDK, license acceptance, `assembleDebug`, `actions/upload-artifact` producing `app-debug-apk`
- **Status:** Completed — workflow triggers on every push to `main`
- **Reference:** `0ee011a`

## 2026-11-18 — CI Build Failure Fixes (1–9)
1. Removed `chmod -R 777 $ANDROID_HOME` (permission denied on runner)
2. Removed `actions/setup-android` (repository not found); used `/usr/local/lib/android/sdk`
3. Added `gradlew` + `gradle/wrapper/gradle-wrapper.properties`
4. `include ":app"` → `include(":app")` — Function invocation expected
5. AGP moved from `plugins {}` to `buildscript { classpath(...) }` — Plugin Portal could not resolve `com.android.application:8.5.0`
6. `gradlew` shebang `#!/system/bin/sh` → `#!/bin/bash` — bad interpreter on Linux runner
7. `task clean(type: Delete)` → `tasks.register("clean", Delete::class)` — syntax error under Gradle 9.x
8. `buildConfigField(...)` string interpolation → extracted `val youtubeApiKey` first — syntax error
9. Removed `shrinkResources = true` — unresolved reference under this AGP/Gradle combo
- **Status:** Completed — each fix pushed individually and verified to move the build to the next error
- **Reference:** commits `7218413`, `993116a`, `b8b75ad`, `30204c4`, `b75e202`

## 2026-11-18 — CI Failure #10: BuildConfig Feature Disabled
- **Error:** `defaultConfig contains custom BuildConfig fields, but the feature is disabled.`
- **Fix applied locally:** `buildFeatures { compose = true; buildConfig = true }` plus `packaging { }` (Gradle 9 renamed `packagingOptions`) and `delete(layout.buildDirectory)` in the root build file
- **Status:** **PENDING — fix written but not yet committed or pushed, and not yet verified**
- **Reference:** local working tree, run `44235241650`

## 2026-11-18 — Project Zip Created
- **Feature:** `power-rangers-tv.zip` at the workspace root (106 KB, 31 files)
- **Rule applied:** mirrors the disk exactly — nothing added, nothing restored
- **Status:** Completed — file verified readable and listed
- **Reference:** `power-rangers-tv.zip`

## 2026-11-18 — Public GitHub Repository Created
- **Feature:** `Otiahaill/power-rangers-tv` created via `gh repo create --public`; initial commit pushed; branch `main`
- **Note:** history was force-pushed once during the restart (`24947f4 → 8463551`) to discard the scrapped implementation
- **Status:** Completed — repo live, CI triggering on every push
- **Reference:** `https://github.com/Otiahaill/power-rangers-tv`

## 2026-11-18 — UHOS Bootstrap Completed (8 Phases)
- **Feature:** 5 global governance files at the workspace root; 7 memory files per project; `templates/PROJECT_TEMPLATE/`; recovery verification (12/12 steps); final integrity audit
- **Status:** Completed — all files verified on disk
- **Reference:** `AGENT_INSTRUCTIONS.md`, `RECOVERY.md`, `WORKSPACE_REGISTRY.md`, `TOOLING.md`, `WORKSPACE_CHANGELOG.md`

## 2026-11-17 — Source Repository Analysed
- **Feature:** `Otiahaill/power-rangers-broadcast` (private web app) analysed; YouTube Data API v3 key verified (61 playlists, 2 live streams); broadcast scheduling algorithm ported to Kotlin; reusable skill extracted
- **Status:** Completed — verification passed
- **Reference:** `.harness/memory/you-tv-app-skill.md`

## Security Changes
- API key moved out of source control into `gradle.properties.local` (gitignored) and `BuildConfig`; cached at runtime in `EncryptedSharedPreferences` under an Android Keystore master key.
- `.gitignore` excludes `gradle.properties.local`, `local.properties`, `*.keystore`, `*.jks`, build outputs and `*.apk`.

## Dependency Updates
- AGP 8.5.0, Kotlin 1.9.23, Compose BOM 2024.06.00, Compose compiler 1.5.14, Hilt 2.48, Retrofit 2.9.0, DataStore 1.0.0, security-crypto 1.1.0-alpha06, Room 2.6.1, Glide 4.16.0, androidx.webkit 1.8.0

## Architecture Changes
- Package renamed `com.example.tvapp` → `com.example.prtv`; layered structure introduced (data / di / security / ui / viewmodel) after the restart.

## Known Issue Documentation
- `data/`, `di/`, `res/values/`, `res/drawable/splash_background.xml`, `res/mipmap-anydpi-v26/` and `app/proguard-rules.pro` are **absent from disk** — the app cannot compile until they are recreated. The user asked that the zip mirror disk exactly, so they are not in the zip.
- The CI secret `YOUTUBE_API_KEY` may not exist on the repository yet.
- `gradle.properties.local` is not automatically loaded by Gradle — CI currently writes it, which needs verification against `project.findProperty`.
- Local on-device builds are blocked: the harness `gradle` binary's shebang points at a path the app UID cannot resolve.
