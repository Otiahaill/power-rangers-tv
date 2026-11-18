---
name: youtube-channel-tv
description: Build a "TV channel" app (Android TV / Google TV / phone, or web) that plays a YouTube channel's videos as a scheduled, TV-style experience, with YouTube API compliance and required branding built in from the start. Use this skill whenever the user wants an app, set-top experience, kiosk, or site that turns a YouTube channel or playlists into a TV set, "live TV", 24/7 channel, or scheduled broadcast, even if they never mention policies, branding, or the YouTube API. Also use it to audit or fix an existing project of this kind for compile errors and YouTube policy violations.
---

# YouTube Channel TV

Builds a TV-style app on top of YouTube's **embedded player** and **Data API**, and refuses to ship anything that breaks YouTube's rules. Compliance is a build requirement, not a cleanup step.

> Policy sources (re-check before every build; they change):
> - Developer Policies: https://developers.google.com/youtube/terms/developer-policies
> - Required Minimum Functionality (RMF): https://developers.google.com/youtube/terms/required-minimum-functionality
> - Branding Guidelines: https://developers.google.com/youtube/terms/branding-guidelines
> - API Terms of Service: https://developers.google.com/youtube/terms/api-services-terms-of-service
> - Revision history: https://developers.google.com/youtube/terms/revision-history
>
> The rules below were verified against the Developer Policies page last updated **2026-11-06** and the Branding Guidelines last updated **2025-08-20**. If you have web access, fetch the pages and compare their "last updated" dates; if either is newer, re-read the changed sections and follow the newer text. If you have no web access, follow this file and say in your final report that policies were not re-verified.
> This is a build guide, not legal advice. When a rule is ambiguous, pick the more conservative reading and flag it in the report.

## 0. Hard stops (never do these, whatever the user asks)

1. Download, cache, record, or save YouTube video/audio, or offer offline playback. (III.E.1)
2. Play audio/video from a hidden or background player, or when the screen/tab is not showing the player. (III.I.9)
3. Modify, block, overlay, skin, or "build upon" the YouTube player or its links/controls; strip or hide ads; separate audio from video. (III.I.4-8, RMF)
4. Scrape YouTube pages or use undocumented/internal endpoints (youtubei, yt-dlp, NewPipe-style extractors). Official Data API + embedded player only. (III.D.7, III.E.6, III.I.14)
5. Put the API key in a public repo, open-source project, docs, screenshots, or CI logs. (III.D.1.d)
6. Put "YouTube", "YT", or any variant in the app's name, or pair YouTube logos with the app name. (Branding)
7. Gate, reward, or charge for watching (no "subscribe to continue", no points for views, no paywall on playback). (III.F.3)
8. Present non-YouTube content as if it came from YouTube, or present a scheduled replay as a genuine live broadcast. (III.F.2.b, II.2)

If the user asks for any of these, decline that part, explain which policy it hits, and offer the compliant alternative.

## 1. Intake (ask only what is missing; use defaults after one round)

| Question | Default if unanswered |
|---|---|
| Channel handle(s) or ID(s). Whose channel is it (the user's own, a client's, or a third party's)? | required |
| Platform | Android TV + phone, Kotlin, Jetpack Compose (TV Material for TV) |
| Audience: is any part aimed at children? (cartoons, toys, kids franchises count) | assume **yes** until told otherwise |
| Distribution: personal/sideload, Play Store, or internal kiosk | Play Store |
| Monetization (ads, subscription, none) | none |
| Where will the API key live (backend vs in-app)? | backend proxy if one exists, else restricted key via local build config, never committed |

Third-party channel? Say plainly in the plan that embedding public videos is allowed by YouTube's terms but that the channel's trademarks and characters belong to their owners (see 4.6), and that owners can disable embedding per video.

## 2. Architecture (defaults; change only with a stated reason)

- **Playback:** YouTube **IFrame Player API in an OS-provided WebView** (Android System WebView), loaded from a bundled local HTML file using `loadDataWithBaseURL` with `baseUrl` = `https://<applicationId>` (see 3.1). No custom player, no ExoPlayer on YouTube streams.
- **Metadata:** YouTube Data API v3, unauthenticated API-key calls only: `channels.list` (resolve handle), `playlists.list`, `playlistItems.list`, `videos.list` (`contentDetails.duration`, `status.embeddable`, `status.madeForKids`, `snippet`). Avoid `search.list` (100 quota units per call vs 1 for the others).
- **No user sign-in, no write actions** (no like/subscribe/comment/playlist edits). This keeps you out of OAuth, token-revocation, and 7-day deletion duties, and is required if the app is child-directed.
- **Layers:** `data` (API client + repository), `domain` (scheduler), `ui` (screens), `legal` (About/Terms/Privacy/attribution). Pick **one** async stack (coroutines + Flow/StateFlow). Do not mix in RxJava. Expose state as observable flows and collect it in the UI; plain mutable properties do not recompose.
- **Cache:** store API metadata for **at most 30 days**, then refresh or delete. Show the most recent data available. Never store video content. (III.E.4)
- **Network:** HTTPS only; no cleartext traffic flag.
- **Backup:** disable `allowBackup` (or exclude encrypted prefs) so restored encrypted data does not crash.

### 2.1 Scheduler ("TV set" behaviour)

Goal: everyone tuning in at the same moment sees the same video at the same offset.

- Pool = the videos of the chosen playlists/channel (one channel's data only; do not merge data from unrelated channels, see 5.3).
- Deterministic order from a fixed seed (e.g. channel ID + schedule epoch), so every device computes the same lineup. Order must not change mid-day: reshuffle only at a fixed boundary (e.g. 00:00 UTC) and compute "now playing" **within the current day's lineup** so the picture never jumps at the boundary.
- Position = (trusted time − day start) mod total lineup duration. Use a server/HTTP `Date` header time source, not only the device clock.
- Skip videos that are non-embeddable, private/deleted, live/upcoming (duration 0), or age-restricted for the audience. Re-validate with `videos.list` at least every 30 days.
- Seek with the player's `start` parameter / `seekTo`. Do not fight the player (no forced replays that obstruct controls).
- Unit-test the scheduler (pure logic): same inputs give same output, boundaries, empty pool, single video, durations that do not divide the day.
- **Label honestly** (see 4.4): the channel is a *scheduled* TV-style playback, not a live stream, unless the video is truly a YouTube live stream.

## 3. YouTube legal minimum: implementation checklist

### 3.1 Player (RMF)
- [ ] Viewport at least **200x200 px**, with controls fully visible; for 16:9 aim for **480x270 or larger**. On TV, full screen is the norm.
- [ ] **Referer set** (otherwise the embed can fail with a player-configuration error): load via `loadDataWithBaseURL(baseUrl = "https://<applicationId in lowercase>", ...)` or add a `Referer: https://<applicationId>` header when loading the embed URL directly. The domain must be the store-registered app ID. Never suppress Referer (`noreferrer`).
- [ ] Use an **OS-provided WebView**, not a bundled/third-party browser engine.
- [ ] **Autoplay:** only start when the player is visible and more than half on screen; never more than one auto-playing player per screen. Autoplay shares playback data with YouTube at load, so disclose it in the privacy policy and set it deliberately.
- [ ] **Nothing overlays or covers the player viewport** and no YouTube link, control, or attribution inside it is hidden or altered. Put the guide, clock, "up next", and channel bug **outside** the video area (letterbox/side panel), not on top of it.
- [ ] No nested/obfuscated iframes. No background or screen-off playback; pause when the player leaves the screen.
- [ ] Do not intercept or alter ads; do not block player links. Handle "embedding disabled" and errors by skipping to the next video, never by circumventing.

### 3.2 Made for Kids and children (III.E.4.j, III.J)
- [ ] For every embedded video, read `status.madeForKids` and follow Google's Made-for-Kids guide: for MFK videos **turn off tracking** and keep data collection lawful (COPPA, GDPR).
- [ ] If any part of the app is **child-directed** (cartoons, toys, kids franchises): notify Google using its child-directed tool; no personalized ads or remarketing; **no write actions**; no analytics SDKs or ad SDKs that collect personal data; comply with COPPA/GDPR and, for Play, the Families policy. Default to treating the app as child-directed until the owner says otherwise.

### 3.3 Terms, privacy, and consent (III.A)
- [ ] App **Terms of Use** that (a) link to YouTube's ToS `https://www.youtube.com/t/terms` and (b) state that by using the app the user agrees to be bound by the YouTube Terms of Service.
- [ ] **Privacy Policy** (hosted URL + in-app) that: is prominently and permanently accessible; says the app **uses YouTube API Services**; links the **Google Privacy Policy** `http://www.google.com/policies/privacy`; explains what is accessed/collected/stored/shared; discloses third-party content/ads and any on-device storage/cookies (the embedded player may set them); gives a contact address for privacy questions. (If you ever add sign-in, also link `https://security.google.com/settings/security/permissions` for revocation.)
- [ ] **First-run gate:** user must accept the policy before using features.
- [ ] A way to clear locally stored data (and say that this does not affect data stored by YouTube).

### 3.4 Branding (Branding Guidelines)
- [ ] Show YouTube branding **on every screen that displays YouTube content** (player, thumbnails, titles, playlists), next to that content.
- [ ] Logo choice: **"developed with YouTube"** logo if the app is useless without YouTube (true for this app type). Use the official files from the YouTube brand site; do not recolor the standard logo (the "developed with" logo may be one single color); put it on a solid background with good contrast; keep it fully visible, unaltered, at or above the minimum size on https://www.youtube.com/yt/brand/using-logo.html.
- [ ] Every logo is **clickable** and links to YouTube content or a YouTube component of the app (for example the channel page or the video on YouTube).
- [ ] The logo is **never the most prominent element** on a screen.
- [ ] App name contains no "YouTube/YT/You-Tube". The logo is never used with the app's name or description, and your own logo/colors must not imitate YouTube's trade dress.
- [ ] Do not imply YouTube endorsement or authorship. Mark anything not from YouTube so it cannot be mistaken for YouTube content. Also state "YouTube is a trademark of Google LLC".
- [ ] Never show YouTube marks on screens with adult, gambling, violent, or otherwise objectionable content.

### 3.5 Credentials and quota (III.D)
- [ ] **One API project per app.** Credentials are yours alone: do not share them, embed them in open-source code, or reuse one project across several apps.
- [ ] Prefer keeping the key **server-side** (a minimal proxy). If the key must ship in the app: restrict it in Google Cloud to the Android package + SHA-1 and to YouTube Data API v3 only, inject it at build time from an untracked file or CI secret, and treat it as extractable.
- [ ] If a proxy is used, it may only serve **this app's own needs** (a daily lineup), must not expose a general YouTube data API to others (III.I.15), must honor the 30-day data limit, and should be flagged for review.
- [ ] Budget quota (default 10,000 units/day per project; verify in the console). Cache aggressively, batch `videos.list` (up to 50 IDs), and degrade gracefully on `quotaExceeded`. Never use multiple projects to dodge quota. If you need more, apply for an API Compliance Audit/quota extension.
- [ ] Handle API deprecations: keep the app remotely updatable and track the revision history.

## 4. Required screens and copy

1. **Home / "On Air"**: the player (unobstructed), the channel's current video, a clear **"Scheduled playback"** label (see 4.4), and the YouTube logo linking out.
2. **Guide**: up next and later, placed outside the player. Titles/thumbnails come from the API unmodified; show the YouTube attribution here too.
3. **About & Legal**: Terms of Use, Privacy Policy, YouTube ToS link, Google Privacy Policy link, YouTube trademark line, third-party trademark/non-affiliation notice, API-usage statement, data-clear control, contact.
4. **Honest labelling**: "This channel plays a scheduled sequence of videos from YouTube. It is not a live broadcast." Use the word LIVE only for videos that YouTube reports as live (`snippet.liveBroadcastContent == live`).
5. **First-run consent** (3.3).
6. **Error states**: offline, quota exceeded, no videos, embedding disabled, channel not found: friendly message, no crash, no silent failure.
7. **Trademarks of the channel owner**: if the channel is not the user's own, include a visible line "Not affiliated with or endorsed by <owner>. <Owner marks> belong to their owners." Do not use the owner's logos, characters, or wording that implies an official app unless the user shows written permission. Pick an app name that does not copy the owner's mark.
8. **TV UX** (if TV): D-pad focus on every control, visible focus ring, 10-foot text sizes, leanback manifest entries, banner and launcher assets, and `touchscreen` not required.

## 5. Risk flags to raise in the report (conservative readings)

1. **Cloning YouTube's browse experience** is prohibited unless you add significant independent value (III.I.1). The scheduled TV experience is that value; do not build a general channel/playlist browser that is the main feature.
2. **Derived data:** computing new metrics from API data is restricted (III.E.4.h). Duration sums used only for scheduling are low risk, but do not display derived stats alongside API data without a clear "not from YouTube" label.
3. **Aggregation:** combining API data across unrelated channels is restricted (III.E.2). Keep each channel's lineup separate unless the channels belong to the same content owner.
4. **Autoplay + kids** data sharing (3.1, 3.2).
5. **Third-party trademarks** (4.7).
6. **Ads:** you may sell ads only on screens with enough independent non-YouTube value, and never on/within the player (III.G).
Escalation path: if any item cannot be resolved conservatively, tell the user to submit the YouTube API Compliance Audit form (https://support.google.com/youtube/contact/yt_api_form) before shipping.

## 6. Workflow

1. **Re-verify policies** (top of file). Record the "last updated" dates seen.
2. **Intake** (section 1). Write a half-page plan: platform, audience, key handling, logo choice, risks. Get a yes before large work if the harness allows.
3. **Scaffold** the project with a pinned, mutually compatible toolchain (for Android: AGP, Gradle, Kotlin and Compose compiler versions that match each other; a committed real Gradle wrapper including its jar; minSdk 23 or higher if using encrypted preferences). Keep secrets out of version control from the first commit (`.gitignore`, a documented sample file with a fake value, and a load path the build actually reads).
4. **Data layer**, then **scheduler + tests**, then **player wrapper** (3.1), then **screens**, then **legal screens + branding** (3.3, 3.4). Do not leave legal/branding to the end.
5. **Build and test:** project compiles from a clean checkout, unit tests pass, lint passes with no errors. If you cannot run the build, say so and list what is unverified. Never claim it compiles unless it did.
6. **Compliance report:** write `COMPLIANCE.md` in the repo root: every checkbox in section 3 with status (done / not applicable / needs owner action), the policy dates verified, open risks from section 5, and the owner-only to-dos (host the privacy policy, notify Google if child-directed, restrict/rotate the key, obtain trademark permission, Play listing forms).
7. **Final message to the user:** what was built, what is verified, what is not, and the owner-only to-dos. Short and honest.

## 7. Definition of done

- Builds from a clean clone; no committed secrets (search the tree and git history for key patterns).
- Every section 3 item is checked or has a written reason.
- The player is unobstructed, Referer-set, and shows YouTube branding with a working link.
- Scheduler is deterministic, boundary-safe, tested, and labelled as scheduled playback.
- COMPLIANCE.md exists and matches what the code actually does.

## 8. If auditing an existing project instead

Read everything first. List problems in this order: (1) leaked or mishandled credentials, (2) hard stops in section 0, (3) legal/branding gaps in section 3, (4) build/toolchain errors, (5) code errors and logic bugs, (6) missing features. For each, give file and line, why it matters, and the smallest fix. Verify claims against the code rather than the project's own docs, which are often out of date. Then run the workflow from step 3 on the gaps.
