# Technical Constraints

- Min SDK API 31. Use `ProviderProperties.Builder` (API 31+). No deprecated raw-int overload. This is a hard floor: `MockLocationService.setupTestProvider()` calls `ProviderProperties.Builder()` unconditionally with no `Build.VERSION.SDK_INT` gate or fallback to the deprecated pre-31 `ProviderProperties` constructor — lowering minSdk below 31 requires adding that dual code path (untested here; the other API-34 manifest declaration, `FOREGROUND_SERVICE_TYPE_LOCATION`, is already `SDK_INT`-gated via `ServiceCompat.startForeground` and is not itself a floor-raiser).
- No Play Services. MapLibre, not Google Maps. No Firebase.
- Offline-first. Core features work without internet. OSRM opt-in, degrades gracefully.
- No `Thread.sleep()`. Use `delay()` in coroutines.
- No empty catch blocks. Every `catch` must log or handle.
- No `GlobalScope`. Use `viewModelScope`, `lifecycleScope`, or scoped `CoroutineScope`.
- No memory leaks. Every `WindowManager.addView` needs matching `removeView` in `onDestroy`. Every scope cancelled in `onDestroy`/`onCleared`.
- Location updates at 1 Hz.
- Battery: use `IMPORTANCE_LOW` notification channel. Wake locks used only when necessary for essential background operations. `MockLocationService` holds `PARTIAL_WAKE_LOCK` while spoofing is active (state != IDLE) — foreground service alone doesn't guarantee the `delay()`-based update loop fires reliably under Doze/Adaptive Battery throttling on some devices (e.g. Android 15 Pixel) once the screen locks. Released on stop/idle/onDestroy.

## Android 9 (API 28) Support — Feasibility (investigated, not implemented)

Investigated: minSdk could move from 31 to 28 additively (SDK_INT dual
paths), with existing API 31+ behavior unchanged, EXCEPT the compass
tracking sub-feature (@docs/features/tap-to-walk.md, "Compass
Orientation") would be unavailable below API 30 — no code change makes
it possible there.

Two call sites currently block API 28, both fixable with a dual path:

| Call site | API floor | Fallback |
|---|---|---|
| `MockLocationService.setupTestProvider()` — `ProviderProperties.Builder()` + `addTestProvider(String, ProviderProperties)` | 31 | Pre-31: deprecated `addTestProvider(String, boolean requiresNetwork, boolean requiresSatellite, boolean requiresCell, boolean hasMonetaryCost, boolean supportsAltitude, boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy)` overload, called with `requiresNetwork=false, requiresSatellite=false, requiresCell=false, hasMonetaryCost=false, supportsAltitude=true, supportsSpeed=true, supportsBearing=true, powerRequirement=ProviderProperties.POWER_USAGE_HIGH, accuracy=ProviderProperties.ACCURACY_FINE` (same values as the current Builder call), wrapped in `@Suppress("DEPRECATION")`. Gate: `if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { <current Builder path> } else { <deprecated overload> }`. |
| `CompassAccessibilityService.captureHeading()` — `takeScreenshot(int, Executor, TakeScreenshotCallback)` | 30 | No fallback below API 30. Gate the whole compass-tracking toggle: `COMPASS_TRACKING_ENABLED` setting row and `CompassAccessibilityService` binding become a no-op (setting hidden or disabled) when `Build.VERSION.SDK_INT < Build.VERSION_CODES.R`. Rest of tap-to-walk (Tier 1 + Tier 2 overlay) is untouched — it has no API 30+ calls. |

Not blockers, verified by audit: `ServiceCompat.startForeground` (already
gated), `PendingIntent.FLAG_IMMUTABLE` (API 23), notification channels
(API 26), `POST_NOTIFICATIONS` (no-op pre-33), `NsdManager` (API 16 surface
used), `WindowManager` overlay (API 26).

Unverified without an actual build: whether MapLibre 13.2.0, CameraX
1.6.1, Room 2.8.4, Hilt 2.56.2, or zxing 3.5.3 merge in a manifest
`minSdkVersion` above 28. First step of any real implementation: drop
`minSdk` to 28 in `LjLibraryConventionPlugin.kt`/`LjApplicationConventionPlugin.kt`
and run a manifest-merge-only build (e.g.
`./gradlew :app:processDebugManifest`) before writing any Kotlin fallback
code — a dependency floor fails fast here for free.

Testing impact: the `addTestProvider` fallback is a direct
`LocationManager` call — per `docs/testing.md`, `LocationManager.addTestProvider`
is out of unit-test scope (requires a real device + Developer Options)
regardless of which overload is used. No new unit test is possible for
that path; it stays "untested" the same way the current API 31 call is.
The compass-tracking SDK_INT gate is a one-line branch and can be covered
by a small unit test on the gating condition alone if/when implemented.

This section documents feasibility only. `minSdk` remains 31. Implementing
this (two SDK_INT branches + the compass-gate carve-out + the manifest-merge
check) is separate follow-up work, not done here.