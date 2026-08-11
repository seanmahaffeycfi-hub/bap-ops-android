# BAP Ops — Punch List

Running list of known gaps, bugs, and small feature requests across both repos (`bap-ops-android` and `bap-ops-django`). Not a full project history — see each repo's own `PROJECT_STATUS.md`/`DJANGO_PROJECT_STATUS.md` for that. This file is just the punch list.

Started: August 10, 2026

## Open items

- [ ] **Receipt images are not uploaded during sync.** The photo is captured and saved locally on the phone (Room stores the local file path), but `SyncManager.syncExpenses()` currently hardcodes `receipt_image_url = null` — the actual file is never sent anywhere. Needs: a file upload mechanism (likely a separate multipart endpoint on the Django side, since DRF's default JSON serializer doesn't handle file uploads), and Django's `Expense.receipt_image_url` field probably needs to become a real `ImageField`/`FileField` rather than a plain `URLField` that nothing populates. Storage location also still needs deciding (see Django status file — Supabase Storage plan is stale now that the project moved to local SQLite).
- [ ] **No way to review or delete unsynced entries from the Android app.** Not required for core function, but useful for catching and removing "naughty" bad entries (e.g. the amount-precision issue) without needing to dig into the database directly. The Room DAOs already have `delete()` functions defined for every entity — just no UI screen calls them yet. Would need: a simple list screen per domain (or one combined screen) showing unsynced rows with a delete button per row.
- [ ] **Store receipt images in the Django backend once upload is built.** `Expense.receipt_image_url` is currently a plain `URLField` that nothing populates — needs to become a real `ImageField`/`FileField` so uploaded photos are actually saved to disk (or wherever the final storage decision lands) and associated with the correct `Expense` row in the database, not just referenced by an unused URL string.

## Resolved (for reference)

- ~~Amount rejected on sync~~ — confirmed root cause: a specific local record had too many total digits (exceeded Django's `max_digits=10` on the `amount` field), unrelated to the earlier decimal-precision fix. Fixed by editing the bad value directly via Android Studio's Database Inspector (View → Tool Windows → App Inspection). Not a recurring bug — just one bad test record.
- ~~Cleartext HTTP traffic blocked~~ — fixed via `android:usesCleartextTraffic="true"` in the manifest.
- ~~`<unknown ssid>` detected instead of real Wi-Fi name~~ — fixed by rewriting `WifiChecker` to use `ConnectivityManager` + `NetworkCallback` with `FLAG_INCLUDE_LOCATION_INFO`, replacing the deprecated `WifiManager.getConnectionInfo()` approach. (Also requires the phone's Location permission to be set to **Precise**, not just granted — this has intermittently reset across reinstalls during testing.)
- ~~`DisallowedHost` error on the Django admin/API~~ — fixed by adding the dev machine's LAN IP to `DJANGO_ALLOWED_HOSTS` in `.env`.
- ~~Entry field text unreadable~~ — fixed by moving off `Theme.AppCompat.DayNight` (was inheriting dark-mode text colors against a forced-light background) and explicitly setting `android:editTextColor`/`android:textColorHint`.