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