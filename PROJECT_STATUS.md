# BAP Ops — Project Status / Handoff Notes

Last updated: August 5, 2026

This file exists so a future Claude session can pick up exactly where this one left off. Keep this file at the root of `bap-ops-android` and update it as work progresses.

## ✅ CURRENT STATE: BUILD IS WORKING

The project compiles and runs successfully as of this update. The Expense entry screen (camera capture, on-device OCR autofill, manual entry, save to Room) is functional end-to-end on-device. This resolves a very long build-breakage saga — full root cause and fix documented below so it's never re-debugged from scratch again.

## What this project is

A four-module system for tracking a 1099 business + the Bloom Again Project (BAP) nonprofit's operations:

1. **EXPENSES** — Receipt photo → on-device OCR autofill, or manual entry. Tag as 1099 or 501. Checkbox for car expense. Entries made on Android, stored locally until synced. **Built and working.**
2. **DONATIONS** — Log value, description, date, donor. Generate a donor receipt. **Not started.**
3. **INVENTORY** — Log vases received (date, POC name/facility/phone/email, recipient) and vases returned (how many, from where). **Not started.**
4. **AUTO** — Mileage tracking. Start/end odometer, tag trip as 1099 or 501, GPS start/end location. Cannot reuse the last reported ending mileage as the next entry's starting mileage. **Not started.**

**Web dashboard** (Django, separate from the Android app, not yet started) needs to show:
- a. Total vases | vases out | vases in | lost or broken vases
- b. Who has how many vases, delivered oldest-first
- c. Auto data: date, start, end, miles, plus a yearly mileage total broken down by 1099 vs 501
- d. Expenses: date, description, amount, with totals for 1099, 501, and car expenses. Filterable and sortable table.

## Key decisions already made (don't re-litigate these)

- Fully separate project from `records_app` (the user's existing Django + Kotlin mileage/donation/inventory tracker). Not a replacement.
- OCR: on-device Google ML Kit (`com.google.mlkit:text-recognition`, bundled model) — offline-capable.
- Data storage: new tables in the **existing CFE Supabase project** (same Postgres instance as the CFE budget app), not a new project.
- Receipt images: new Supabase Storage bucket in that same project (not yet created).
- Repos: `bap-ops-android` (this repo) + a not-yet-created Django repo, proposed name `bap-ops`.
- Sync: local Room DB first, syncs only on home Wi-Fi (SSID-gated, not yet implemented).
- User wants full copy-paste-ready code for entire files, not fragments (except genuine 1-2 line changes). Near-zero prior Android Studio experience — needs precise, literal UI navigation instructions, exact full file paths, and screenshots confirmed at each step. Has occasionally pasted content into the wrong file/tab — always worth double-checking the editor tab name matches the file being discussed before assuming a paste landed correctly.

## Environment / tooling notes (important — this stack is very new)

This project sits on bleeding-edge tooling (all released in the first half of 2026), which caused most of the debugging saga below. Current confirmed-working versions:

- **AGP 9.2.1**
- **Kotlin 2.2.10**, using the **classic `org.jetbrains.kotlin.android` plugin** — NOT AGP 9's new built-in Kotlin. Two flags in `gradle.properties` opt out of built-in Kotlin and its new DSL:
  ```
  android.newDsl=false
  android.builtInKotlin=false
  ```
  **These are temporary, officially-documented escape hatches that Google has stated will be removed in AGP 10.0** (expected mid-2026). This project will eventually need real migration work off of them — not urgent now, but don't be surprised when a future AGP upgrade forces this.
- **KSP 2.3.3** — KSP1 no longer exists in this version; don't attempt `ksp.useKSP2=false`, it fails immediately at configuration time.
- **Room 3.0.1** (new `androidx.room3` package, not `androidx.room`). Requires Kotlin-only code, suspend/Flow-based DAOs, and an explicit SQLite driver dependency (`androidx.sqlite:sqlite-framework`, using `AndroidSQLiteDriver()` in the database builder).
- **compileSdk / targetSdk 37** (bumped from 36 mid-project when local tooling moved to API 37).
- **CameraX 1.6.0**, **ML Kit Text Recognition 16.0.1** (bundled/offline model), **Play Services Location 21.3.0**, **Retrofit 2.11.0**, **Lifecycle 2.11.0**.
- Kotlin/Java JVM target explicitly pinned to 11 in both `compileOptions` (Java) and a `kotlin { compilerOptions { jvmTarget.set(...) } }` block (Kotlin) in `app/build.gradle.kts` — needed once the classic Kotlin plugin was introduced, since it defaulted to a different JVM target than the existing Java setting and Gradle refuses mismatched targets.

## THE BIG BUG: Room 3.0's enum handling — full story, so it's never re-debugged

For a long stretch, every build failed at the KSP annotation-processing step with:
```
e: [ksp] [MissingType]: Element 'com.seanmahaffey.bapops.data.AppDatabase' references a type that is not present
KSP failed with exit code: PROCESSING_ERROR
```
This message is extremely unhelpful — it names `AppDatabase` even when the real fault lies elsewhere, and gives no file/line number. It took extensive binary-search isolation (temporarily stripping `AppDatabase.kt` down to one entity at a time, removing the DAO, removing the converter registration, one variable at a time) to actually find it.

**Root cause:** the project had a hand-written `Converters.kt` class using `@TypeConverter`-annotated methods to convert the `RecordType` enum to/from a `String` column, registered on `AppDatabase` via `@TypeConverters(Converters::class)` — the completely standard, textbook Room pattern that's worked in Room 2.x for years. Room 3.0 renamed `@TypeConverter` to `@ColumnTypeConverter`. But that rename alone wasn't even the full story — **Room 3.0 also has built-in automatic enum handling** (storing enums as their string name) that makes a custom converter for a plain enum like `RecordType` entirely unnecessary. The mere presence of a custom converter class — even after correctly renaming the annotation to `@ColumnTypeConverter` — still broke the build with the same opaque error.

**The actual fix:** delete the custom `Converters.kt` entirely. Do not register any `@TypeConverters` annotation on `AppDatabase` for the `RecordType` enum. Room 3.0 handles it automatically. If this project ever needs a *genuinely* custom converter for some other type in the future (not a plain enum), remember: the annotation is now `@ColumnTypeConverter`, imported from `androidx.room3.ColumnTypeConverter` — but for enums specifically, try leaving Room to its own devices first before writing one.

**Secondary bug found immediately after, also now fixed:** `com.google.mlkit.vision.text.TextRecognizerOptions` doesn't exist at that import path — it's nested one level deeper, at `com.google.mlkit.vision.text.latin.TextRecognizerOptions`. One-line import fix.

## What's built and confirmed working

- Android Studio project scaffolded, pushed to GitHub, package `com.seanmahaffey.bapops`.
- **Full Room 3.0 database layer**, confirmed compiling and running: entities (`Expense`, `Donation`, `VaseReceived`, `VaseReturned`, `MileageEntry`), DAOs for each, `RecordType.kt` enum, `AppDatabase.kt` (with all 5 entities/DAOs registered, no custom converter). All in `app/src/main/java/com/seanmahaffey/bapops/data/`. **`Converters.kt` was deleted — do not recreate it for the `RecordType` enum.**
- `ExpenseViewModel.kt` and `ExpenseEntryActivity.kt` — camera capture via CameraX, on-device OCR autofill via ML Kit (best-effort heuristic parsing: description from receipt's first line, amount from a "total"-containing line or largest dollar figure found, date from first `MM/DD/YYYY`-style pattern), manual entry fields, 1099/501 radio buttons, car-expense checkbox, save to Room. **Confirmed working end-to-end on the user's physical Samsung SM-S908U device.**
- `activity_expense_entry.xml` layout.
- `activity_main.xml` and `MainActivity.kt` — Add Expense button launches `ExpenseEntryActivity`.
- `AndroidManifest.xml` — camera, GPS, network permissions all declared correctly.

## Not started yet

- Donations, Vases, and Auto UI screens.
- No Wi-Fi-SSID sync-gating logic.
- No Retrofit API calls implemented (dependency is present, unused).
- Django backend repo does not exist yet.
- Supabase Storage bucket for receipts does not exist yet.
- Render web service for the new Django app does not exist yet.

## Suggested next steps

Two reasonable directions, not yet chosen — pick up the conversation by asking the user which they'd like:
- **Continue on Android:** build the Donations, Vases, and Auto screens next, following the same pattern established in the Expense screen (ViewModel + Activity + layout + Room save). These should go faster than Expenses did, since none of them need the camera/OCR complexity, and the Room layer is already proven solid.
- **Switch to Django:** scaffold the `bap-ops` backend repo and models, so the API payload shape and Room schema can be designed together before building the sync layer.