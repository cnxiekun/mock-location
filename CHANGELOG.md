# 更新日志

本项目为 [locationjoystick](https://github.com/shortcuts/locationjoystick) 的个人改造版，日志从改造版首次发布起记录。原作者的迭代历史见上游仓库。

## 1.0.0（2026-08-18）

改造版首次正式发布。功能与原版一致，针对国内使用环境做了个人定制：

### 新增 / 适配

- 应用显示名改为「定位模拟」，包名独立为 `com.locationjoystick.app.cn`，可与原版共存安装。
- 地图瓦片切换为高德地图（国内可达），代码内做 WGS-84 ↔ GCJ-02 坐标转换对齐。
- 地图搜索改为高德地理编码：API Key 由用户在设置中填写（设置 → 定位与 GPS → 高德地图 API Key），不写进代码；点击搜索按钮或键盘搜索键才发起搜索，多结果列表展示。
- 默认位置校准为深圳 · 中国海外大厦（WGS-84）。
- 菜单栏恢复为点右上角「×」关闭（`gesturesEnabled = false`），不再响应滑动或点击外部关闭。
- 传送（Teleport）改为立即推送位置，消除切换位置时瞬间闪回真实 GPS 的问题。
- 切到后台不再强制跳回首页，返回时停留在退出前的页面。
- 修复 JDK 21 编译时的 JVM target 不一致，及 R8 构建内存溢出（Gradle 内存上限调大）。

### 发布方式

- 构建产物用专属 keystore（`xiekun-location-release.keystore`）签名，信息见 `xiekun-location-密钥备忘.txt`。
- 版本号从 `1.0.0` 起，APK 发布到 GitHub Releases，不再依赖原作者的 release-please 自动发布流程。
