# BAP Ops — Project Status / Handoff Notes

Last updated: August 3, 2026

This file exists so a future Claude session can pick up exactly where this one left off. Add this file to the root of the `bap-ops-android` repo and update it as work progresses.

## What this project is

A four-module system for tracking a 1099 business + the Bloom Again Project (BAP) nonprofit's operations:

1. **EXPENSES** — Receipt photo → on-device OCR autofill, or manual entry. Tag as 1099 or 501. Checkbox for car expense. Entries made on Android, stored locally until synced.
2. **DONATIONS** — Log value, description, date, donor. Generate a donor receipt.
3. **INVENTORY** — Log vases received (date, POC name/facility/phone/email, recipient) and vases returned (how many, from where).
4. **AUTO** — Mileage tracking. Start/end odometer, tag trip as 1099 or 501, GPS start/end location. Cannot reuse the last reported ending mileage as the next entry's starting mileage (forces sequential entry, no gaps).

**Web dashboard** (Django, separate from the Android app) needs to show:
- a. Total vases | vases out | vases in | lost or broken vases
- b. Who has how many vases, delivered oldest-first
- c. Auto data: date, start, end, miles, plus a yearly mileage total broken down by 1099 vs 501
- d. Expenses: date, description, amount, with totals for 1099, 501, and car expenses. Filterable and sortable table.

## Key decisions already made (don't re-litigate these)

- **This is a fully separate project from `records_app`** (the user's existing Django + Kotlin/Room/ViewModel/LiveData mileage/donation/inventory tracker). Not a replacement, not an extension. Keep them independent.
- **OCR approach:** on-device, using Google ML Kit (`com.google.mlkit:text-recognition`, the *bundled* model, not the Play-Services-linked unbundled version) — works fully offline, no cloud cost, no first-run download dependency.
- **Data storage:** New tables live inside the **existing CFE Supabase project** (same Postgres instance that backs the CFE budget app on Render) — not a new Supabase project, not the "other barely used" project. Django table names are namespaced by app label so there's no collision with CFE's `cashflow_*`/`bills_*`/`income_*` tables.
- **Receipt images:** Will go in a new Supabase Storage bucket (not yet created) in that same project. Free tier is 1GB storage / 500MB DB / 2 project limit — worth keeping an eye on usage over time.
- **Repo strategy:** Two separate GitHub repos, matching the precedent already set with `records_app` / `records_app_android`:
  - `bap-ops-android` (Kotlin, Android Studio) — **created, this is the repo this file lives in**
  - Django backend repo — **not yet created**, proposed name was `bap-ops` (open to renaming)
- **Sync architecture:** Android app writes to local Room DB first. Syncs to the Django backend only when connected to a specific (home) Wi-Fi network — same network-gated pattern used in `records_app`. **Exact SSID-detection mechanism not yet designed/coded.**
- **Render:** New Django app will need its own Render web service, pointed at the same Supabase `DATABASE_URL` host as CFE, but as a separate deploy/codebase.

## Current state of `bap-ops-android`

- Android Studio project created and pushed to GitHub (`seanmahaffeycfi-hub/bap-ops-android`, branch `main`).
- Package name / applicationId: `com.seanmahaffey.bapops`
- Project name: `BAPOps`
- Language: Kotlin, minSdk 26, targetSdk 36, compileSdk 36
- **Important environment note:** This project is on **AGP 9.2.1**, which introduced *built-in Kotlin support* — there is no separate `org.jetbrains.kotlin.android` plugin declared, and none should be added. This is very new tooling (AGP 9.0 shipped Jan 2026), so if anything Kotlin/Gradle-related seems to behave unexpectedly, check current official Android docs before assuming old patterns apply — training-data knowledge of Android tooling predating 2026 is likely stale here.
- Gradle dependencies added and **synced successfully** (BUILD SUCCESSFUL):
  - **Room 3.0.1** (`androidx.room3` — note the new package name, this is Room 3.x not 2.x, all DAO methods must be Kotlin `suspend` functions or return `Flow`, Kotlin-only code gen, requires KSP)
  - **KSP 2.3.3** (required for Room 3 annotation processing; kapt does not work with AGP 9 built-in Kotlin)
  - **CameraX 1.6.0** (camera-core, camera-camera2, camera-lifecycle, camera-view) — for receipt photo capture
  - **ML Kit Text Recognition 16.0.1** (bundled/offline model) — for OCR
  - **Play Services Location 21.3.0** — for GPS mileage tracking
  - **Lifecycle 2.11.0** (viewmodel-ktx, livedata-ktx, runtime-ktx)
  - **Retrofit 2.11.0 + OkHttp 4.12.0 logging interceptor** — for the eventual sync-to-Django step
  - **Kotlin Coroutines 1.10.2** (android + play-services)
- `AndroidManifest.xml` updated with permissions: CAMERA, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, INTERNET, ACCESS_NETWORK_STATE, ACCESS_WIFI_STATE, plus a camera `<uses-feature>` declaration.
- Two harmless KSP/built-in-Kotlin warnings appear on sync (`Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin`) — build succeeds despite them. If they ever escalate into real errors, the documented fix is adding `android.disallowKotlinSourceSets=false` to `gradle.properties` — **not yet applied, no need to unless something actually breaks.**
- Nothing has been committed since the dependency additions — **next action is to `git add . && git commit && git push` the current Gradle state if that hasn't been done yet.**

## Not started yet

- No Room entities, DAOs, or database class written for any of the 4 domains.
- No UI screens/activities/fragments beyond the default `MainActivity` scaffold.
- No camera capture or OCR-parsing code.
- No GPS/location capture code.
- No Wi-Fi-SSID-detection / sync-gating logic.
- No Retrofit API interface or sync logic.
- Django backend repo does not exist yet.
- Django models for the 4 domains do not exist yet.
- Supabase Storage bucket for receipts does not exist yet.
- Render web service for this new Django app does not exist yet.

## Decision point left open at end of session

Two reasonable next steps were discussed, not yet chosen:
- **Option A:** Build the Room database layer (entities + DAOs) for the 4 domains next, since it's the foundation everything else in the Android app depends on, and the offline-first design means much of the app (manual entry, OCR capture) can be built and tested before Django exists at all.
- **Option B:** Scaffold the Django backend repo and models first, so API payload shape and Room schema can be designed together and stay in sync.

No final call was made — ask the user which they'd like to start with, or suggest Option A (Room layer, starting with EXPENSES) as the default if they want a recommendation, consistent with the reasoning above.

## User working style notes (carried over, still relevant)

- Wants full copy-paste-ready code for entire files, not fragments/diffs (unless it's genuinely a 1-2 line change).
- Near-zero prior Android Studio experience — needs precise, literal UI navigation instructions (exact menu paths, what to click, what a given panel/icon actually is) rather than assumed familiarity. Has been sending screenshots to confirm each step; expect that to continue.
- Comfortable and fast in Django/VS Code from the CFE project — that side of the stack can move faster with less hand-holding.
- Catches inconsistencies and asks precise follow-up questions — no need to over-explain, but don't skip steps either.
