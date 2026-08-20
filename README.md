# 模拟定位（Location Joystick 个人改造版）

![minSdk](https://img.shields.io/badge/minSdk-28%20(Android%209)-green?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-purple?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

在 Android 上模拟你的 GPS 位置。通过悬浮摇杆、保存的路线或自动漫游，把手机「挪」到地图上任意一点，其他 App 照常运行。

本仓库是对 [@shortcuts](https://github.com/shortcuts/locationjoystick) 的开源项目 **Location Joystick** 的个人改造版，功能与原作者一致，针对国内使用环境做了适配（见下方「个人定制」）。

## 个人定制

| 项目 | 说明 |
|------|------|
| 应用名称 | **模拟定位** |
| 应用 ID | `com.cnxiekun.mocklocation`（与原版 `com.locationjoystick.app` 可共存安装） |
| 地图瓦片 | 高德地图（国内可达，代码内做 WGS-84 ↔ GCJ-02 坐标转换对齐） |
| 地图搜索 | 高德地理编码，**API Key 由用户在设置中填写**（设置 → 定位与 GPS → 高德地图 API Key），不写进代码 |
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
| **位置模拟** | 将 GPS 位置模拟到地图任意地点，**在技术层面实现了“远程位置签到”的可行性**。 |
| **导入/导出** | 全部数据（路线、收藏、速度档位、控件配置、漫游默认、抖动设置）以 JSON 导出/导入。路线还支持 GPX、GPS Joystick、YAMLA 格式。「重置所有数据」一键清空。 |
| **后台服务** | 前台服务保持模拟运行，即使最小化或锁屏也不中断，低优先级通知。 |
| **新手引导** | 首次运行自动弹出定位 + 通知权限请求，并引导设置悬浮窗、模拟 GPS。 |
| **组队同步** | 同一 Wi-Fi 下多台设备同步模拟位置，无需账号。一台为主（通过二维码加入的会话分享位置），其他为从（跟随主的位置）。 |
| **点按行走** | 两个快捷方式：悬浮地图点按直接行走（跳过确认弹窗）；屏幕点按覆盖层——在游戏或地图 App 内点任意位置即行走过去。可调米/像素比例。 |
| **深链接** | 把任意坐标或收藏分享为链接，点开即进入 App 并居中到该点，带确认弹窗（传送 / 行走 / 沿路行走）。也注册为 Google 地图和 `geo:` 链接的处理方。 |
| **主题** | 深色 / 浅色两种配色，设置 → 外观中切换。 |
| **隐藏传送功能** | 可选开关（默认关）：一键隐藏 App 内所有传送按钮，只保留行走和路线回放。 |

## 下载

正式版 APK 在 [GitHub Releases](https://github.com/cnxiekun/mock-location/releases) 页面发布，下载最新版后安装即可（允许安装未知来源应用）。

> 说明：`com.cnxiekun.mocklocation` 与原版 `com.locationjoystick.app` 是独立应用，可以同时安装，互不影响。

## 使用步骤

1. **开启开发者选项**：设置 → 关于手机 → 连点「版本号」7 次。
2. **选择模拟位置应用**：设置 → 系统 → 开发者选项 → 「选择模拟位置信息应用」→ 选择「模拟定位」。
3. **完成首次引导**：首次打开 App 会自动弹出定位和通知权限请求，按提示允许；再按引导设置悬浮窗权限和模拟 GPS 应用。
4. **开始模拟**：打开「模拟定位」→ 点地图传送，或用摇杆/路线/漫游移动 → 切到目标 App，模拟保持后台运行。

> **提示**：部分 App 能检测到模拟位置。请查阅对应 App 社区的现有规避方案。全部核心功能均不需要 root。**本工具仅用于学习和开发测试，请勿用于违反平台规则或公司规定的场景。**

> **关于搜索**：地图搜索走高德地理编码，需要在 **设置 → 定位与 GPS → 高德地图 API Key** 填写你自己的高德 Web 服务 key 后才能使用。不填也能用——可直接在地图上点选或长按定位，只是没有文字搜索。

## 构建

### 环境要求

- JDK 17+（本项目在 JDK 21 下验证通过）
- Android SDK（API 28+），在 `local.properties` 里配置 `sdk.dir` 或通过环境变量 `ANDROID_HOME`
- Gradle Wrapper（无需单独安装 Gradle）

### 调试包

> 说明：`make` 命令是对 Gradle 的简写封装——`make build` 等价于 `./gradlew assembleRelease`，`make build-debug` 等价于 `./gradlew assembleDebug`，直接执行 `./gradlew ...` 效果相同。

```bash
make build-debug
```

调试包位于 `app/build/outputs/apk/debug/`。

### 正式包（签名）

本项目使用专属签名文件 `cnxiekun-mock-location.keystore`（在项目根目录，已被 `.gitignore` 忽略，不要提交）。签名信息（别名 / 密码）记录在 `cnxiekun-mock-location-密钥备忘.txt`。

```bash
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

## 仓库结构

多模块 Gradle 工程（NowInAndroid 风格）。每个功能拆成 `:api`（对外契约）+ `:impl`（实现），公共代码在 `:core:*`。

```
mock-location/
├── app/                        # 应用入口、Hilt 装配、导航、侧边栏
├── build-logic/                # Gradle 约定插件
├── core/
│   ├── common/                 # 常量（AppConstants）、工具、坐标转换
│   ├── data/                   # 仓库（单一数据源）
│   ├── database/               # Room 数据库
│   ├── datastore/              # DataStore 偏好
│   ├── designsystem/           # 设计令牌、主题、共享组件
│   ├── location/               # 模拟 GPS 引擎（前台服务）
│   ├── map/                    # 地图、GeoJSON、MapLibre 桥
│   ├── model/                  # 纯 Kotlin 领域模型
│   ├── overlay/                # 悬浮窗工具
│   ├── routing/                # OSRM 客户端、路线、漫游、回放
│   └── testing/                # 共享测试工具
├── feature/
│   ├── favorites/              # 收藏
│   ├── group/                  # 组队同步
│   ├── joystick/               # 悬浮摇杆
│   ├── map/                    # 地图界面
│   ├── onboarding/             # 新手引导
│   ├── routes/                 # 路线
│   ├── settings/               # 设置
│   └── widget/                 # 悬浮控件
├── docs/                       # 技术参考文档（开发者用）
├── dist/                       # 发布 APK（本地暂存，不上传仓库）
└── *.kts / gradlew / gradle/   # 构建脚本与 Gradle Wrapper
```

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

## ⚠️ 免责声明

本工具仅供**学习、开发和测试**用途，旨在帮助开发者调试基于位置的应用（如地图、LBS 游戏等）。

**请勿将本工具用于以下场景：**
- 违反任何第三方平台（如社交 App、游戏）的用户协议或服务条款
- 违反所在公司、组织的内部管理规定（如虚假打卡、考勤作弊等）

用户因使用本工具产生的任何后果（包括但不限于账号封禁、数据丢失、法律责任等），均由使用者自行承担，与项目作者无关。

**简单说：用之前想清楚，出了事自己兜着。**

## 许可证

MIT License，版权归 cnxiekun 所有。见 [LICENSE](LICENSE)。

本项目基于 [@shortcuts](https://github.com/shortcuts/locationjoystick) 的开源项目 **Location Joystick** 改造，感谢原作者的出色工作。
