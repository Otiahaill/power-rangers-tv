# power-rangers-tv — Study Session

**Last updated:** 2026-11-18 — active working session

## Current Development Objective
Get the GitHub Actions CI build to compile successfully and produce `app-debug.apk`. The project was fully scrapped and restarted on 2026-11-18; this is a fresh codebase, not the old `com.example.tvapp` one.

## What Has Been Completed
- Found the user's private repo `Otiahaill/power-rangers-broadcast` and analysed its YouTube integration (61 playlists, 2 live streams verified with the API key).
- Extracted the reusable skill to `.harness/memory/you-tv-app-skill.md`.
- Completed the full UHOS bootstrap (8 phases): 5 global governance files, 7 memory files per project, `templates/PROJECT_TEMPLATE/`, recovery verification, integrity audit.
- Created public GitHub repos `Otiahaill/power-rangers-tv` and `Otiahaill/power-rangers-tw` via `gh repo create`.
- **Scrapped both native projects** and rebuilt only `power-rangers-tv` from zero with package `com.example.prtv`, Hilt DI, Retrofit, DataStore, Room, security-crypto, 4 screens, 4 ViewModels, BroadcastScheduler, TV Leanback intent filter.
- Implemented the **embedded + Keystore-protected API key** flow the user asked for (no user-supplied key required).
- Stored the API key in `gradle.properties.local` (gitignored) and wired it to `BuildConfig.YOUTUBE_API_KEY`; CI reads it from the `YOUTUBE_API_KEY` Actions secret.
- Fixed 9 successive CI build failures (see project.md § CI Build Failure History).
- Pushed `power-rangers-tv.zip` to the workspace root (106 KB, 31 files — exactly the disk state, no files added).

## What Is Currently Being Implemented
The latest CI failure was:
```
defaultConfig contains custom BuildConfig fields, but the feature is disabled.
To enable the feature, add the following to your module-level build.gradle:
`android.buildFeatures.buildConfig true`
```
**Fix already applied locally** to `app/build.gradle.kts`:
- added `buildConfig = true` inside `buildFeatures`
- renamed `packagingOptions { }` → `packaging { }` (Gradle 9 API)
- root `build.gradle.kts`: `delete(rootProject.buildDir)` → `delete(layout.buildDirectory)`

**These local edits are NOT yet committed or pushed.**

## Files Recently Modified
| File | Change |
|---|---|
| `app/build.gradle.kts` | `buildConfig = true`, `packaging { }`, `buildConfigField` syntax |
| `build.gradle.kts` | `tasks.register("clean", Delete::class)`, `layout.buildDirectory` |
| `gradlew` | shebang `#!/bin/bash` (was `#!/system/bin/sh`) |
| `gradle.properties.local` | holds the API key (gitignored) |
| `.github/workflows/ci.yml` | injects `${{ secrets.YOUTUBE_API_KEY }}` into `gradle.properties.local` |
| `project.md`, `study_session.md`, `changelog.md` | this memory layer, rewritten for the restarted project |

## Important Discoveries
- AGP 8.5.0 is **not** resolvable from the Gradle Plugin Portal — it must come in through a `buildscript { classpath(...) }` block with `google()` in the repositories.
- The GitHub Actions runner ships **Gradle 9.9.1**, which is much newer than AGP 8.5 expects. `shrinkResources`, `packagingOptions`, `rootProject.buildDir` and `task clean(type: …)` all fail on it.
- The runner's Android SDK lives at `/usr/local/lib/android/sdk` and cannot be chmod-ed.
- `gradle.properties.local` is not read by Gradle automatically — only `gradle.properties` or `-P` flags or an explicit `gradle.properties.local` include. CI writes the file, and `project.findProperty("youtubeApiKey")` reads it **only if it is in `gradle.properties`** — **this is an unresolved risk to verify**.
- The user explicitly asked that memory files always capture actions, because the context window may be cleared at any time.

## Current Blockers
- CI has never reached a successful compile yet — 10 consecutive failures, each fixed and pushed, each revealing the next error.
- The `data/`, `di/`, `res/values/` layers are absent from disk (deleted during the restart). The user chose not to restore them, so the app **cannot compile** until those files exist. This must be raised explicitly with the user.
- The CI secret `YOUTUBE_API_KEY` may not exist on the GitHub repo yet — needs creating as a repository secret.

## Errors Encountered
CI failures 1–10 are catalogued in `project.md` § CI Build Failure History. Latest unresolved: `buildConfig` feature disabled (fix applied locally, not yet pushed).

## Tests Performed
- No successful compile yet — every CI attempt has failed at configuration time, never at Kotlin compilation.
- Local build attempts on the phone were blocked: the `gradle` binary in the harness prefix has a `/data/local/tmp/...` shebang that does not resolve for the app UID (`bad interpreter: No such file or directory`).

## Decisions Awaiting Confirmation
- Whether to restore the missing `data/`, `di/`, `res/values/` files (needed for compilation) — user said "don't restore it", but that was in the context of the zip contents, not necessarily the app itself.
- Whether to create the `YOUTUBE_API_KEY` GitHub Actions secret, or commit a CI-only `gradle.properties` with the key (less safe but simpler).

## Immediate Next Action
1. Commit and push the `buildConfig = true` + `packaging { }` fix.
2. Watch the CI run; fix the next error.
3. Raise the missing-`data/di/res` blocker with the user explicitly.

## Exact Commands to Resume Work
```sh
cd /storage/emulated/0/Android/data/com.androidharness.app/files/workspace/power-rangers-tv

# See what is uncommitted
git status
git diff

# Push the pending build config fix
git add app/build.gradle.kts build.gradle.kts
git commit -m "fix: build - enable buildConfig feature for Gradle 9.x"
git push origin main

# Watch CI
gh run list --repo Otiahaill/power-rangers-tv --limit 3
gh run view <run-id> --repo Otiahaill/power-rangers-tv --log-failed

# Rebuild the zip (mirror disk exactly, do not add files)
cd /storage/emulated/0/Android/data/com.androidharness.app/files/workspace
python3 -c "
import zipfile, os
root = 'power-rangers-tv'
skip_dirs = {'.git', 'build', '.gradle'}
with zipfile.ZipFile('power-rangers-tv.zip', 'w', zipfile.ZIP_DEFLATED) as z:
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in skip_dirs]
        for f in filenames:
            if f.endswith(('.apk', '.aab', '.dex', '.class')):
                continue
            full = os.path.join(dirpath, f)
            z.write(full, full)
"
```

## Git Status
- **Branch:** main, tracking `origin/main`
- **Last pushed commit:** `b75e202` — "fix: build - remove shrinkResources which is not available in Gradle 9.x"
- **Uncommitted:** `app/build.gradle.kts` (buildConfig + packaging), `build.gradle.kts` (clean task + layout.buildDirectory)
- **Note:** the repo was force-pushed once during the restart (`24947f4 → 8463551`); the previous history was intentionally discarded.

## Project Stable State
⚠️ **NOT STABLE.** The app does not compile — the `data/`, `di/` and `res/values/` source files are missing from disk. CI has never passed configuration. This session's state is captured here so it survives a context-window reset.
