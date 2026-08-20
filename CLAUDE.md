# mock-location — Agent Reference

> Primary reference for AI coding agents. Read before touching any file.

---

## Project

Android-only mock GPS app. Background operation, minimal battery.

| Field | Value |
|---|---|
| Package | `com.cnxiekun.mocklocation`（与原版 `com.locationjoystick.app` 可共存） |
| Language | Kotlin |
| UI | Jetpack Compose |
| Min SDK | API 28 |
| Distribution | GitHub Releases APK |
| Storage | Room + DataStore |
| Backend | None |
| Open source | Yes |

Constraints:

- Offline-first
- No accounts
- All data on-device in Room + DataStore

---

## Documentation Maintenance Policy

Work is NOT complete until affected docs are updated. These files must stay in sync with the code:

| File | Update when |
|------|-------------|
| `CLAUDE.md` (this file) — feature table | Adding or removing a feature |
| `CLAUDE.md` — module table in `docs/architecture.md` | Adding or removing a Gradle module |
| `CLAUDE.md` — Key Services table | Adding, removing, or renaming a service or singleton |
| `docs/architecture.md` | Module added/removed or architecture pattern changes |
| `docs/domain-models.md` | Any change to `core/model/` data classes or enums |
| `docs/features/<feature>.md` | Behaviour change in the corresponding feature |
| `docs/features/export-import.md` | Any change to `ExportData` fields or import/export scope |
| `README.md` — feature table | Adding or removing a user-visible feature |
| `README.md` — module table | Adding or removing a Gradle module |

Rules:
- New feature → create `docs/features/<feature>.md` AND add row to CLAUDE.md feature table AND README.md feature table.
- New Gradle module → add row to `docs/architecture.md` module table AND README.md module table.
- New domain model or field → update `docs/domain-models.md`.
- Deleted feature/module → remove from all tables above.
- Doc changes go in the same commit as the code change, not a follow-up.

---

## Pre-Commit Validation Policy

Work is NOT complete until lint and test passes.

```bash
make format
make lint
make test
```

Rules:
- Fix every lint error before declaring done. Warnings acceptable; errors not.
- Run after every set of edits, not just end of session.
- If check fails, fix root cause. Don't suppress unless genuine false positive + inline comment explaining why.
- Never suppress `Errors` category rules. Never batch-suppress with `@file:Suppress`.
- Never add co-authoring or "Claude-Sessions" to the commit

---

## Architecture

→ See @docs/architecture.md

---

## Constants

→ See @docs/constants.md

---

## Feature Specifications

→ See @docs/features/

| Feature | Doc |
|---------|-----|
| Mock Location Engine + GPS Realism | @docs/features/mock-location.md |
| Foreground Service | @docs/features/foreground-service.md |
| Floating Joystick | @docs/features/joystick.md |
| Map (MapLibre) | @docs/features/map.md |
| Route System | @docs/features/routes.md |
| Favorite Locations | @docs/features/favorites.md |
| Speed Profiles | @docs/features/speed-profiles.md |
| Floating Widget | @docs/features/widget.md |
| Click-to-Move / Teleport | @docs/features/click-to-move.md |
| Roaming Mode | @docs/features/roaming.md |
| Export / Import | @docs/features/export-import.md |
| QR Share / Transfer | @docs/features/qr-transfer.md |
| Deep Links & Location Sharing | @docs/features/deep-link.md |
| Last Remembered Location | @docs/features/last-location.md |
| Onboarding | @docs/features/onboarding.md |
| Group Sync | @docs/features/group-sync.md |
| Tap to Walk | @docs/features/tap-to-walk.md |
| Theme | @docs/features/theme.md |
| Hide Teleport Features | @docs/features/hide-teleport.md |

---

## Domain Models

→ See @docs/domain-models.md

---

## Key Services

| Service | Module | Type | Purpose |
|---------|--------|------|---------|
| `MockLocationService` | `:core:location` | ForegroundService | Owns `LocationManager` test provider. Exposes `StateFlow<SpoofState>`. Commands: `startSpoofing`, `updatePosition`, `stopSpoofing`. Suspended-phase state held in `AtomicReference<SuspendedPhaseState>`; transitions via `advanceSuspendedPhase()` pure function (testable independently). |
| `JoystickOverlayService` | `:feature:joystick:impl` | Service | Extends `OverlayService`. Manages `WindowManager` overlay. Reads joystick input → `LocationRepository.updatePosition()`. |
| `FloatingWidgetService` | `:feature:widget:impl` | Service | Manages widget overlay. Binds to `MockLocationService`. |
| `RoamingEngine` | `:core:routing` | Class (not service) | Instantiated by `MockLocationService`. Owns OSRM client + random waypoint picker. Runs on service scope. |
| `ReplayOrchestrator` | `:core:location` | Class (not service) | Instantiated by `MockLocationService`. Owns all route-replay and walk-to orchestration (`handleStart`/`handlePause`/`handleResume`/`handleStop`/`handleCancel`) extracted from the service. Communicates back via lambdas (`onStateChange`, `onPositionChange`, `pushLocationUpdate`, etc.) instead of holding its own state directly. |
| `FollowerCatchUpCoordinator` | `:core:location` | Class (not service) | Instantiated by `MockLocationService`. Owns the FOLLOWER-mode catch-up target (`AtomicReference<LatLng?>`) extracted from the service, mirroring the `WalkCoordinator` pattern — state ownership + per-tick step logic (`advance()`) live in one small class instead of scattered `@Volatile` fields on the service. |
| `EphemeralReplayController` | `:core:location` | Class (`@Singleton`) | Owns the walk→ephemeral-replay transition. Injected by both `MapViewModel` and `FloatingWidgetService`. `addWaypoint()` decides whether to start a new ephemeral replay (walk→replay transition) or append to an existing one. Eliminates duplicated state-machine logic across call sites. |
| `WalkCoordinator` | `:core:data` | Class (`@Singleton`) | Thin facade over `WalkToEngine`. Cancels any in-flight walk before starting a new one, forwards position ticks to `LocationRepository`, clears `walkTarget` on arrival or cancellation. |
| `ActivityStateRepository` | `:core:data` | Repository (`@Singleton`) | Single source of truth for unified pause state across all movement modes. Exposes `isActivityPaused: Flow<Boolean>` combining walk-to, route replay, and roaming pause. Prefer over manually combining individual flows from `LocationRepository` and `RoamingRepository`. |
| `TeleportUseCase` | `:core:data` | Class (`@Singleton`) | Single entry point for all teleport operations — fires the update-position intent to `MockLocationService`, persists last location + last teleport time. Injected by both `MapViewModel` and `FavoritesViewModel` so every teleport path shares the same persistence and cooldown logic (`cooldownFor`/`cooldownsFor`). |
| `StartRouteReplayUseCase` | `:core:location` | Class (`@Singleton`) | Starts a route replay: resolves the route's speed profile, optionally teleports to the start waypoint first (via `TeleportUseCase`), then sends the start-replay intent to `MockLocationService`. Dedupes route-replay-start logic previously duplicated in `MapViewModel` and `FloatingWidgetService`. |

---

## Permissions

→ See @docs/permissions.md

---

## Technical Constraints

→ See @docs/technical-constraints.md

---

## Code Style Rules

→ See @docs/code-style.md

---

## Testing Strategy

→ See @docs/testing.md

```bash
make coverage        # generate HTML + XML reports
make coverage-open   # open HTML report in browser
```

---

## 个人定制（2026-08-18）

- 全程使用简体中文回复，所有提示、报错、帮助文档都用中文。
- 应用显示名「模拟定位」，包名 `com.cnxiekun.mocklocation`（与作者原版可共存）。
- 地图瓦片使用高德（国内可达，代码内做 WGS-84 ↔ GCJ-02 坐标转换）；地图搜索用高德地理编码，key 由用户在设置中填写（不写进代码）。
- release 构建用 `cnxiekun-mock-location.keystore` 签名（别名/密码见 `cnxiekun-mock-location-密钥备忘.txt`）。
- 版本号有**两处**，发版时必须同步改，否则系统显示与实际不符：
  1. `build-logic/convention/.../LjApplicationConventionPlugin.kt` 的 `versionName`（系统「应用信息」显示 + versionCode 计算）。
  2. `AppConstants.AppInfo.VERSION_NAME`（App 界面底部显示）。
  3. 改完执行 `make build`，产物复制到 `dist/mock-location-v<版本>.apk`，再到 GitHub Releases 建对应 tag 上传。
- 本仓库是个人改造版，不需要原作者的自动发布流程（release-please 等已移除）。
