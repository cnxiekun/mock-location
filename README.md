# 定位模拟（locationjoystick 个人改造版）

![minSdk](https://img.shields.io/badge/minSdk-28%20(Android%209)-green?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-purple?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

在 Android 上模拟你的 GPS 位置。通过悬浮摇杆、保存的路线或自动漫游，把手机「挪」到地图上任意一点，其他 App 照常运行。

本仓库是对开源项目 [locationjoystick](https://github.com/shortcuts/locationjoystick) 的个人改造版，功能与原作者一致，针对国内使用环境做了适配（见下方「个人定制」）。

## 个人定制

| 项目 | 说明 |
|------|------|
| 应用名称 | **定位模拟** |
| 包名 | `com.cnxiekun.mocklocation`（与原版 `com.locationjoystick.app` 可共存安装） |
| 地图瓦片 | 高德地图（国内可达，代码内做 WGS-84 ↔ GCJ-02 坐标转换对齐） |
| 地图搜索 | 高德地理编码，**API Key 由用户在设置中填写**（设置 → 定位与 GPS → 高德地图 API Key），不写进代码 |
| 版本号 | `1.0.0` |
| 发布 | 本地构建 + 手动上传 GitHub Releases（不依赖原作者的 release-please 自动发布） |

## 功能

| 功能 | 说明 |
|------|------|
| **地图** | MapLibre 渲染高德瓦片（国内可达）。点击传送或长按行走，模拟位置以实时标记显示。 |
| **上次位置** | 重启后自动恢复到上次模拟的位置，无需手动重新输入。 |
| **摇杆** | 悬浮于任意 App 之上的圆形摇杆，拖动即可向任意方向移动，可拖到屏幕任意位置。 |
| **速度档位** | 慢走 / 走 / 跑 / 自行车 / 驾车 5 档预设，全部可自定义。超速时给出反作弊提示。可从悬浮控件快速切换。 |
| **路线** | 在地图上打点生成路线，支持直线段与沿路（OSRM 导航）两种类型。可保存、编辑、回放、循环、实时录制，也可从 GPX 文件导入。 |
| **漫游** | 设定中心点、半径和距离，在范围内随机行走。可选沿路行走、结束后返回起点。通过地图上的底表配置。 |
| **收藏** | 保存命名的位置，一键传送或行走。可从内联对话框或地图拾取器添加。可选内置 26 个热门地点（设置 → 收藏 → 显示热门地点）。 |
| **悬浮控件** | 可配置的快捷面板悬浮于其他 App 之上，折叠为小圆钮，展开为面板，按钮可自定义。 |
| **点击移动** | 长按地图 → 「走到这里」或「传送到这里」。行走按当前速度推进，传送瞬间到达。 |
| **二维码传输** | 同一 Wi-Fi 下通过扫一个二维码（或输入 6 位代码）在两台设备间共享/导入配置。 |
| **GPS 拟真** | 让模拟 GPS 看起来接近真实芯片：静止时保持朝向、海拔漂移、启动精度收敛、卫星数量、信号自然中断等，全部可选开关。 |
| **导入/导出** | 全部数据（路线、收藏、速度档位、控件配置、漫游默认、抖动设置）以 JSON 导出/导入。路线还支持 GPX、GPS Joystick、YAMLA 格式。「重置所有数据」一键清空。 |
| **后台服务** | 前台服务保持模拟运行，即使最小化或锁屏也不中断，低优先级通知。 |
| **新手引导** | 首次运行多步引导：定位权限、悬浮窗权限、启用模拟位置。 |
| **组队同步** | 同一 Wi-Fi 下多台设备同步模拟位置，无需账号。一台为主（通过二维码加入的会话分享位置），其他为从（跟随主的位置）。 |
| **点按行走** | 两个快捷方式：悬浮地图点按直接行走（跳过确认弹窗）；屏幕点按覆盖层——在游戏或地图 App 内点任意位置即行走过去。可调米/像素比例。 |
| **深链接** | 把任意坐标或收藏分享为链接，点开即进入 App 并居中到该点，带确认弹窗（传送 / 行走 / 沿路行走）。也注册为 Google 地图和 `geo:` 链接的处理方。 |
| **主题** | 深色 / 浅色两种配色，设置 → 外观中切换。 |
| **隐藏传送功能** | 可选开关（默认关）：一键隐藏 App 内所有传送按钮，只保留行走和路线回放。 |

## 下载

正式版 APK 在 [GitHub Releases](https://github.com/cnxiekun/mock-location/releases) 页面发布。

侧载安装：

```bash
adb install mock-location-v1.0.0.apk
```

或把 APK 传到手机后用文件管理器打开安装（允许安装未知来源应用）。

> 说明：`com.cnxiekun.mocklocation` 与原版 `com.locationjoystick.app` 是独立应用，可以同时安装，互不影响。

## 使用步骤

1. **开启开发者选项**：设置 → 关于手机 → 连点「版本号」7 次。
2. **选择模拟位置应用**：设置 → 系统 → 开发者选项 → **选择模拟位置应用** → 选择「定位模拟」。
3. **授予悬浮窗权限**：打开「定位模拟」→ 按引导授予悬浮窗权限。
4. **开始模拟**：打开「定位模拟」→ 点地图传送，或用摇杆/路线/漫游移动 → 切到目标 App，模拟保持后台运行。

> **提示**：部分 App 能检测到模拟位置。请查阅对应 App 社区的现有规避方案。全部核心功能均不需要 root。

> **关于搜索**：地图搜索走高德地理编码，需要在 **设置 → 定位与 GPS → 高德地图 API Key** 填写你自己的高德 Web 服务 key 后才能使用（不填写时搜索框会提示）。

## 构建

### 环境要求

- JDK 17+（本项目在 JDK 21 下验证通过）
- Android SDK（API 28+），在 `local.properties` 里配置 `sdk.dir` 或通过环境变量 `ANDROID_HOME`
- Gradle Wrapper（无需单独安装 Gradle）

### 调试包

```bash
./gradlew assembleDebug
# 或
make build-debug
```

调试包位于 `app/build/outputs/apk/debug/`。

### 正式包（签名）

本项目使用专属签名文件 `cnxiekun-mock-location.keystore`（在项目根目录，已被 `.gitignore` 忽略，不要提交）。签名信息（别名 / 密码）记录在 `cnxiekun-mock-location-密钥备忘.txt`。

```bash
./gradlew assembleRelease
# 或
make build
```

正式包位于 `app/build/outputs/apk/release/`。更新版本时务必使用**同一个 keystore**，否则无法覆盖安装。

> ⚠️ keystore 文件与密码请妥善备份，丢失后只能卸载重装。

### 质量检查

```bash
make lint       # Android Lint
make test       # 单元测试（JVM）
make coverage   # 覆盖率报告（HTML + XML）
```

## 架构

多模块、NowInAndroid 风格。每个功能 = Gradle 模块，公共代码在 `:core:*`。

```
feature/*        — UI + ViewModels（Compose 界面，无业务逻辑）
  ↓ 依赖
core/data        — 仓库（单一数据源）
  ↓
core/database    — Room 数据库
core/datastore   — DataStore 偏好

core/location    — 模拟 GPS 引擎（前台服务），与 UI 无关
core/model       — 纯 Kotlin 数据类，无 Android 依赖
```

MVVM + 仓库模式。ViewModel 暴露 `StateFlow`/`SharedFlow`，Compose 通过 `collectAsStateWithLifecycle()` 收集，Hilt 依赖注入。`LjApp` 用 `ModalNavigationDrawer` 包裹 `LjNavHost`，`IdleScreen` 作为引导完成后的主入口，卡片导航到地图 / 路线 / 收藏 / 设置。

### 模块

每个功能拆成 `:api`（对外契约）+ `:impl`（实现）。

| 模块 | 职责 |
|------|------|
| `:app` | 入口、Hilt 装配、`LjApp`、`LjNavHost`、侧边栏 |
| `:core:common` | 工具、扩展、常量（`AppConstants`） |
| `:core:data` | 仓库、DataStore 偏好 |
| `:core:database` | Room 数据库、DAO、实体 |
| `:core:datastore` | DataStore 偏好数据源 |
| `:core:designsystem` | 设计令牌、主题、排版、共享组件 |
| `:core:location` | 模拟 GPS 前台服务 + 移动引擎 |
| `:core:model` | 纯 Kotlin 领域数据类 |
| `:core:map` | GeoJSON 工具、MapLibre 生命周期桥、样式扩展 |
| `:core:overlay` | WindowManager 悬浮窗工具 |
| `:core:routing` | OSRM 客户端、路线插值、漫游引擎、回放引擎 |
| `:core:testing` | 共享测试工具、Fake |
| `:feature:favorites:api` / `:impl` | 收藏列表、地图拾取器、传送 |
| `:feature:group:api` / `:impl` | 组队同步界面——主/从 Wi-Fi 位置同步 |
| `:feature:joystick:impl` | 悬浮摇杆 |
| `:feature:map:api` / `:impl` | MapLibre 界面、地图交互、漫游底表 |
| `:feature:onboarding:api` / `:impl` | 多步新手引导 |
| `:feature:routes:api` / `:impl` | 路线列表、创建、详情、回放 |
| `:feature:settings:api` / `:impl` | 速度档位、控件配置、导入导出、二维码传输 |
| `:feature:widget:impl` | 悬浮控件 + 面板 |

## 技术栈

| 组件 | 方案 |
|------|------|
| 语言 | Kotlin 2.2 |
| UI | Jetpack Compose + Material3 |
| 地图 | MapLibre Android SDK 13.x + 高德瓦片 |
| DI | Hilt (Dagger) |
| 数据库 | Room |
| 偏好 | DataStore (Preferences) |
| 路线导航 | OSRM（router.project-osrm.org） |
| 序列化 | kotlinx-serialization (JSON) |
| 异步 | Kotlin 协程 + Flow |
| 构建 | Gradle + Version Catalog（`libs.versions.toml`） |
| CI | 无（个人改造版，本地构建） |
| 最低系统 | API 28 (Android 9) |

## 许可证

MIT License。见 [LICENSE](LICENSE)。
