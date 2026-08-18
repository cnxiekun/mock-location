package com.locationjoystick.feature.settings.impl

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.component.LjCheckboxRow
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.model.AppFeature
import com.locationjoystick.core.model.FeatureSurface
import com.locationjoystick.core.model.SpeedProfile
import com.locationjoystick.core.model.ThemeMode
import kotlin.math.roundToInt

@Composable
internal fun SettingsMenusSubScreen(
    uiState: SettingsUiState,
    isRooted: Boolean,
    onNavigateBack: () -> Unit,
    isSpoofing: Boolean,
    onToggleSpoofing: () -> Unit,
    locationLabel: String? = null,
    onAction: (SettingsAction) -> Unit,
    onCheckCompassService: () -> Unit = {},
    bottomBar: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) onCheckCompassService()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LjScaffold(
        title = "菜单",
        isSpoofing = isSpoofing,
        onToggleSpoofing = onToggleSpoofing,
        locationLabel = locationLabel,
        onNavigationClick = onNavigateBack,
        navigationIcon = LjIcons.ArrowBack,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        actions = { SubScreenActions(uiState.isDirty, onAction) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(remember { ScrollState(0) })
                                .padding(16.dp),
                    ) {
                        ThemeSection(uiState, onAction)
                        Spacer(Modifier.height(24.dp))
                        AppFeaturesSection(uiState, isRooted, onAction)
                        Spacer(Modifier.height(24.dp))
                        SpeedCycleSection(uiState, onAction)
                        Spacer(Modifier.height(24.dp))
                        TapToWalkSection(uiState, onAction)
                        Spacer(Modifier.height(24.dp))
                        PrivacySection(uiState, onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Text("外观", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(4.dp))
    Text(
        "切换到浅色主题，在明亮/阳光充足的条件下更清晰易读。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "浅色模式",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = uiState.themeMode == ThemeMode.LIGHT,
            onCheckedChange = { light ->
                onAction(SettingsAction.SetThemeMode(if (light) ThemeMode.LIGHT else ThemeMode.DARK))
            },
        )
    }
}

@Composable
private fun TapToWalkSection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    var showWarning by rememberSaveable { mutableStateOf(false) }
    val enabled = uiState.floatingMapQuickWalk || uiState.tapToWalkOverlayEnabled

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("点击行走", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                "BETA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        "点击地图上的位置即可行走——无需确认。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "启用点击行走",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = enabled,
            onCheckedChange = { on ->
                if (on) {
                    showWarning = true
                } else {
                    onAction(SettingsAction.SetFloatingMapQuickWalk(false))
                    onAction(SettingsAction.SetTapToWalkOverlayEnabled(false))
                }
            },
        )
    }
    if (enabled) {
        Spacer(Modifier.height(12.dp))
        Text(
            "地图比例（%.2f 米/像素）——请将游戏完全缩小以获得最佳精度".format(uiState.tapToWalkScaleMpx),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Slider(
            value = uiState.tapToWalkScaleMpx.toFloat(),
            onValueChange = { v ->
                onAction(
                    SettingsAction.SetTapToWalkScaleMpx(
                        v.toDouble().coerceIn(
                            AppConstants.TapToWalkConstants.MIN_SCALE_MPX,
                            AppConstants.TapToWalkConstants.MAX_SCALE_MPX,
                        ),
                    ),
                )
            },
            valueRange = AppConstants.TapToWalkConstants.MIN_SCALE_MPX.toFloat()..AppConstants.TapToWalkConstants.MAX_SCALE_MPX.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        // takeScreenshot(int, Executor, TakeScreenshotCallback) requires API 30 — no fallback exists.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Spacer(Modifier.height(16.dp))
            CompassOrientationSection(uiState, onAction)
        }
    }
    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text("启用点击行走？") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("拦截触摸的屏幕覆盖层可能会增加在某些游戏中被检测的风险。请自行承担使用风险。")
                    Text("精度取决于比例设置——请在游戏中缩小以获得更好的效果。")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showWarning = false
                    onAction(SettingsAction.SetFloatingMapQuickWalk(true))
                    onAction(SettingsAction.SetTapToWalkOverlayEnabled(true))
                }) { Text("仍然启用") }
            },
            dismissButton = {
                TextButton(onClick = { showWarning = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun PrivacySection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Text("隐私", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(4.dp))
    LjCheckboxRow(
        checked = uiState.hideTeleportFeatures,
        onCheckedChange = { onAction(SettingsAction.SetHideTeleportFeatures(it)) },
        title = "隐藏传送功能",
        description =
            "移除应用中所有传送选项——地图、收藏、" +
                "路线、群组同步和小部件。仅保留行走和路线回放。",
    )
    Spacer(Modifier.height(8.dp))
    LjCheckboxRow(
        checked = uiState.hideWidgetOverlay,
        onCheckedChange = { onAction(SettingsAction.SetHideWidgetOverlay(it)) },
        title = "隐藏悬浮小部件",
        description = "在模拟定位运行期间，不显示悬浮小部件按钮。",
    )
    Spacer(Modifier.height(8.dp))
    LjCheckboxRow(
        checked = uiState.showRouteJumpButtons,
        onCheckedChange = { onAction(SettingsAction.SetShowRouteJumpButtons(it)) },
        title = "显示路线跳转按钮",
        description =
            "在路线回放控件中添加“上一个航点 / 下一个航点”按钮，" +
                "用于在航点之间即时传送。默认关闭。",
    )
}

@Composable
private fun CompassOrientationSection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    val context = LocalContext.current
    var showCalibration by rememberSaveable { mutableStateOf(false) }

    Text("指南针方向", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(4.dp))
    Text(
        "启用后，应用会在每次行走前检测地图的北方方向以校正目标位置。" +
            "需要无障碍服务。注意：某些游戏会检测无障碍服务。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("无障碍服务", style = MaterialTheme.typography.bodyLarge)
            Text(
                if (uiState.isCompassServiceGranted) "已启用" else "未启用——点击打开安卓设置",
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (uiState.isCompassServiceGranted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
        if (!uiState.isCompassServiceGranted) {
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
            }) { Text("打开设置") }
        }
    }
    if (uiState.isCompassServiceGranted) {
        Spacer(Modifier.height(8.dp))
        LjCheckboxRow(
            checked = uiState.compassTrackingEnabled,
            onCheckedChange = { onAction(SettingsAction.SetCompassTrackingEnabled(it)) },
            title = "检测指南针方向",
            description = "在每次行走前读取红色指北针，以校正目标位置。",
        )
        if (uiState.compassTrackingEnabled) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showCalibration = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(LjIcons.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("校准指南针区域")
            }
        }
    }
    if (showCalibration) {
        CompassCalibrationDialog(
            cx = uiState.compassRegionCxPct,
            cy = uiState.compassRegionCyPct,
            radius = uiState.compassRegionRadiusPct,
            onConfirm = { cx, cy, radius ->
                onAction(SettingsAction.SetCompassRegion(cx, cy, radius))
                showCalibration = false
            },
            onDismiss = { showCalibration = false },
        )
    }
}

@Composable
private fun CompassCalibrationDialog(
    cx: Float,
    cy: Float,
    radius: Float,
    onConfirm: (Float, Float, Float) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentCx by remember { mutableFloatStateOf(cx) }
    var currentCy by remember { mutableFloatStateOf(cy) }
    var currentRadius by remember { mutableFloatStateOf(radius) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("校准指南针区域") },
        text = {
            Column {
                Text(
                    "将圆圈拖到游戏屏幕中指南针出现的位置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                PhoneScreenPreview(
                    cx = currentCx,
                    cy = currentCy,
                    radius = currentRadius,
                    onPositionChange = { newCx, newCy ->
                        currentCx = newCx
                        currentCy = newCy
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text("圆圈大小", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = currentRadius,
                    onValueChange = { currentRadius = it },
                    valueRange = 0.02f..0.2f,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentCx, currentCy, currentRadius) }) { Text("完成") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun PhoneScreenPreview(
    cx: Float,
    cy: Float,
    radius: Float,
    onPositionChange: (Float, Float) -> Unit,
) {
    val circleColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        onPositionChange(
                            (down.position.x / size.width).coerceIn(0f, 1f),
                            (down.position.y / size.height).coerceIn(0f, 1f),
                        )
                        drag(down.id) { change ->
                            change.consume()
                            onPositionChange(
                                (change.position.x / size.width).coerceIn(0f, 1f),
                                (change.position.y / size.height).coerceIn(0f, 1f),
                            )
                        }
                    }
                },
    ) {
        val circleCx = cx * size.width
        val circleCy = cy * size.height
        val circleRadius = radius * size.minDimension
        drawCircle(
            color = circleColor.copy(alpha = 0.25f),
            center = Offset(circleCx, circleCy),
            radius = circleRadius,
        )
        drawCircle(
            color = circleColor,
            center = Offset(circleCx, circleCy),
            radius = circleRadius,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

private data class FeatureMeta(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val isRootGated: Boolean = false,
)

private fun featureMeta(feature: AppFeature): FeatureMeta =
    when (feature) {
        AppFeature.MAP_FLOATING -> {
            FeatureMeta("地图快捷方式", "打开一个紧凑的地图视图，无需切换到主应用。", LjIcons.LocationOn)
        }

        AppFeature.JOYSTICK_TOGGLE -> {
            FeatureMeta("显示/隐藏摇杆", "打开或关闭悬浮摇杆覆盖层。", LjIcons.Visibility)
        }

        AppFeature.JOYSTICK_LOCK -> {
            FeatureMeta(
                "锁定摇杆",
                "松开后摇杆会继续朝最后按住的方向移动。",
                LjIcons.Lock,
            )
        }

        AppFeature.FAVORITES -> {
            FeatureMeta("收藏", "传送到已保存的位置或走过去。", LjIcons.Favorite)
        }

        AppFeature.ROUTES -> {
            FeatureMeta("路线", "列出已保存的路线并开始回放。", LjIcons.Route)
        }

        AppFeature.ROAMING -> {
            FeatureMeta("漫游", "配置并开始在一定半径内随机行走。", LjIcons.Explore)
        }

        AppFeature.SEARCH -> {
            FeatureMeta("搜索", "按名称查找并跳转到某个地点。", LjIcons.Search)
        }

        AppFeature.SPEED_CYCLE -> {
            FeatureMeta("速度循环", "一键在所有速度方案之间循环切换。", LjIcons.Speed)
        }
    }

private val FEATURE_ROW_HEIGHT = 64.dp
private val FEATURE_ROW_SPACING = 8.dp

@Composable
private fun AppFeaturesSection(
    uiState: SettingsUiState,
    isRooted: Boolean,
    onAction: (SettingsAction) -> Unit,
) {
    Text("应用功能", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "选择哪些快捷功能出现在悬浮小部件和地图屏幕中，" +
            "并可拖拽重新排序。两个界面默认共享相同顺序。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth().padding(start = 48.dp), horizontalArrangement = Arrangement.End) {
        Text("小部件", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
        Text("地图", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
    }

    val order = uiState.featureOrder
    val rowHeightPx = with(LocalDensity.current) { (FEATURE_ROW_HEIGHT + FEATURE_ROW_SPACING).toPx() }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragDeltaY by remember { mutableStateOf(0f) }

    Column(verticalArrangement = Arrangement.spacedBy(FEATURE_ROW_SPACING)) {
        order.forEachIndexed { index, feature ->
            val isDragging = draggingIndex == index
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 1f else 0f)
                        .let { mod ->
                            if (isDragging) {
                                mod.graphicsLayerTranslationY(dragDeltaY)
                            } else {
                                mod
                            }
                        },
            ) {
                FeatureRow(
                    feature = feature,
                    isRooted = isRooted,
                    uiState = uiState,
                    onAction = onAction,
                    dragModifier =
                        Modifier.pointerInput(feature) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingIndex = index
                                    dragDeltaY = 0f
                                },
                                onDragEnd = {
                                    val targetIndex =
                                        (index + (dragDeltaY / rowHeightPx).roundToInt()).coerceIn(0, order.lastIndex)
                                    if (targetIndex != index) {
                                        val newOrder = order.toMutableList()
                                        val moved = newOrder.removeAt(index)
                                        newOrder.add(targetIndex, moved)
                                        onAction(SettingsAction.SetFeatureOrder(newOrder))
                                    }
                                    draggingIndex = null
                                    dragDeltaY = 0f
                                },
                                onDragCancel = {
                                    draggingIndex = null
                                    dragDeltaY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragDeltaY += dragAmount.y
                                },
                            )
                        },
                )
            }
        }
    }
}

private fun Modifier.graphicsLayerTranslationY(ty: Float): Modifier = this.then(Modifier.graphicsLayer { translationY = ty })

@Composable
private fun SpeedCycleSection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Text("速度循环", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "选择小部件“速度循环”按钮循环切换哪些速度方案。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Column {
        SpeedProfile.defaultProfiles().forEach { profile ->
            val checked = profile.id in uiState.enabledSpeedProfileIds
            LjCheckboxRow(
                checked = checked,
                title = profile.name,
                onCheckedChange = { isChecked ->
                    val updated = uiState.enabledSpeedProfileIds.toMutableSet()
                    if (isChecked) updated.add(profile.id) else updated.remove(profile.id)
                    onAction(SettingsAction.SetEnabledSpeedProfileIds(updated))
                },
            )
        }
    }
}

@Composable
private fun FeatureRow(
    feature: AppFeature,
    isRooted: Boolean,
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    dragModifier: Modifier,
) {
    val meta = featureMeta(feature)
    val rowEnabled = !meta.isRootGated || isRooted
    Row(
        modifier = Modifier.fillMaxWidth().height(FEATURE_ROW_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LjIcons.DragHandle,
            contentDescription = "拖拽以重新排序${meta.label}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = dragModifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = meta.icon,
            contentDescription = null,
            tint = if (rowEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(
                text = meta.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (rowEnabled) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            Text(
                text = meta.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (meta.isRootGated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (FeatureSurface.WIDGET in feature.surfaces) {
            Checkbox(
                checked = feature in uiState.enabledWidgetFeatures,
                enabled = rowEnabled,
                modifier = Modifier.width(56.dp).semantics { contentDescription = "${meta.label}（小部件）" },
                onCheckedChange = { checked ->
                    val updated = uiState.enabledWidgetFeatures.toMutableSet()
                    if (checked) {
                        updated.add(feature)
                    } else {
                        updated.remove(feature)
                    }
                    onAction(SettingsAction.SetWidgetFeatures(updated))
                },
            )
        } else {
            Checkbox(
                checked = false,
                enabled = false,
                modifier = Modifier.width(56.dp),
                onCheckedChange = {},
            )
        }
        if (FeatureSurface.MAP in feature.surfaces) {
            Checkbox(
                checked = feature in uiState.enabledMapFeatures,
                modifier = Modifier.width(56.dp).semantics { contentDescription = "${meta.label}（地图）" },
                onCheckedChange = { checked ->
                    val updated = uiState.enabledMapFeatures.toMutableSet()
                    if (checked) updated.add(feature) else updated.remove(feature)
                    onAction(SettingsAction.SetMapFeatures(updated))
                },
            )
        } else {
            Checkbox(
                checked = false,
                enabled = false,
                modifier = Modifier.width(56.dp),
                onCheckedChange = {},
            )
        }
    }
}
