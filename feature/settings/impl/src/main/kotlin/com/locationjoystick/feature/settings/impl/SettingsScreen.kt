package com.locationjoystick.feature.settings.impl

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.component.AppIcon
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.location.rememberSpoofToggleState
import com.locationjoystick.core.model.RoamingDefaults

private enum class SettingsSection { GPS, MENUS, FAVORITES_ROUTES, ROAMING }

private sealed class PendingImport {
    data class File(
        val uri: android.net.Uri,
    ) : PendingImport()

    data class QrData(
        val data: com.locationjoystick.core.model.ExportData,
    ) : PendingImport()

    data class GpsJoystick(
        val uri: android.net.Uri,
    ) : PendingImport()

    data class Yamla(
        val uri: android.net.Uri,
    ) : PendingImport()
}

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onOpenDrawer: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val amapKey by viewModel.amapKey.collectAsStateWithLifecycle()
    val roamingDefaults by viewModel.roamingDefaults.collectAsStateWithLifecycle()
    val isRooted by viewModel.isRooted.collectAsStateWithLifecycle()
    val spoofToggle = rememberSpoofToggleState()
    val context = LocalContext.current
    var pendingImport by remember { mutableStateOf<PendingImport?>(null) }
    var showQrShare by remember { mutableStateOf(false) }
    var qrExportSession by remember { mutableStateOf<SettingsViewModel.QrExportSession?>(null) }
    var showQrScanner by remember { mutableStateOf(false) }
    var showEnterCodeDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userFeedback.collect { feedback ->
            if (feedback.isError) {
                val result =
                    snackbarHostState.showSnackbar(
                        message = feedback.message,
                        actionLabel = "报告",
                        duration = androidx.compose.material3.SnackbarDuration.Long,
                    )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    val intent =
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(com.locationjoystick.core.common.constants.AppConstants.AppInfo.GITHUB_ISSUES_URL),
                        )
                    context.startActivity(intent)
                }
            } else {
                snackbarHostState.showSnackbar(
                    message = feedback.message,
                    duration = androidx.compose.material3.SnackbarDuration.Short,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.qrImportReady.collect { exportData ->
            showQrScanner = false
            pendingImport = PendingImport.QrData(exportData)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.qrExportReady.collect { session ->
            qrExportSession = session
            showQrShare = true
        }
    }

    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(AppConstants.ExportConstants.MIME_TYPE),
        ) { uri ->
            if (uri != null) viewModel.writeExportToUri(uri)
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) pendingImport = PendingImport.File(uri)
        }

    val importGpsJoystickLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) pendingImport = PendingImport.GpsJoystick(uri)
        }

    val importYamlaLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) pendingImport = PendingImport.Yamla(uri)
        }

    val pending = pendingImport
    if (pending != null) {
        ImportConfirmDialog(
            onReplace = {
                when (pending) {
                    is PendingImport.File -> viewModel.importSettings(pending.uri, replace = true)
                    is PendingImport.QrData -> viewModel.importSettings(pending.data, replace = true)
                    is PendingImport.GpsJoystick -> viewModel.importFromGpsJoystick(pending.uri, replace = true)
                    is PendingImport.Yamla -> viewModel.importFromYamla(pending.uri, replace = true)
                }
                pendingImport = null
            },
            onAdd = {
                when (pending) {
                    is PendingImport.File -> viewModel.importSettings(pending.uri, replace = false)
                    is PendingImport.QrData -> viewModel.importSettings(pending.data, replace = false)
                    is PendingImport.GpsJoystick -> viewModel.importFromGpsJoystick(pending.uri, replace = false)
                    is PendingImport.Yamla -> viewModel.importFromYamla(pending.uri, replace = false)
                }
                pendingImport = null
            },
            onDismiss = { pendingImport = null },
        )
    }

    val qrImportFetching by viewModel.qrImportFetching.collectAsStateWithLifecycle()

    if (showQrScanner) {
        QrScannerScreen(
            onQrScanned = viewModel::onQrScanned,
            onPermissionDenied = { showQrScanner = false },
            onNavigateBack = { showQrScanner = false },
            isFetching = qrImportFetching,
        )
        return
    }

    val exportSession = qrExportSession
    if (showQrShare && exportSession != null) {
        QrShareDialog(
            qrText = exportSession.qrText,
            code = exportSession.code,
            onDismiss = {
                viewModel.stopQrExport()
                showQrShare = false
                qrExportSession = null
            },
        )
    }

    if (showEnterCodeDialog) {
        EnterExportCodeDialog(
            onDismiss = { showEnterCodeDialog = false },
            onConfirm = { code ->
                showEnterCodeDialog = false
                viewModel.onExportCodeEntered(code)
            },
        )
    }

    if (showResetConfirm) {
        ResetAllDataConfirmDialog(
            onConfirm = {
                showResetConfirm = false
                viewModel.resetAllData()
            },
            onDismiss = { showResetConfirm = false },
        )
    }

    SettingsScreen(
        uiState = uiState,
        roamingDefaults = roamingDefaults,
        amapKey = amapKey,
        isRooted = isRooted,
        hotLocationTree = viewModel.hotLocationTree,
        hotRouteTree = viewModel.hotRouteTree,
        onOpenDrawer = onOpenDrawer,
        isSpoofing = spoofToggle.isSpoofing,
        onToggleSpoofing = spoofToggle.onToggle,
        locationLabel = spoofToggle.locationLabel,
        onCheckCompassService = { viewModel.checkCompassServiceGranted() },
        onAction = { action ->
            when (action) {
                is SettingsAction.SetSpeed -> {
                    viewModel.setSpeed(action.id, action.displaySpeed)
                }

                is SettingsAction.SetSpeedUnit -> {
                    viewModel.setSpeedUnit(action.unit)
                }

                is SettingsAction.SetWidgetFeatures -> {
                    viewModel.setWidgetFeatures(action.features)
                }

                is SettingsAction.SetEnabledSpeedProfileIds -> {
                    viewModel.setEnabledSpeedProfileIds(action.ids)
                }

                is SettingsAction.SetMapFeatures -> {
                    viewModel.setMapFeatures(action.features)
                }

                is SettingsAction.SetFeatureOrder -> {
                    viewModel.setFeatureOrder(action.order)
                }

                is SettingsAction.SetRememberLastLocation -> {
                    viewModel.setRememberLastLocation(action.enabled)
                }

                is SettingsAction.SetMapFollowsLocation -> {
                    viewModel.setMapFollowsLocation(action.enabled)
                }

                is SettingsAction.SetJitterIdleRadius -> {
                    viewModel.setJitterIdleRadius(action.meters)
                }

                is SettingsAction.SetJitterMovingRadius -> {
                    viewModel.setJitterMovingRadius(action.meters)
                }

                is SettingsAction.SetJitterIntervalSeconds -> {
                    viewModel.setJitterIntervalSeconds(action.seconds)
                }

                is SettingsAction.SetJitterIdleIntervalSeconds -> {
                    viewModel.setJitterIdleIntervalSeconds(action.seconds)
                }

                is SettingsAction.SetRealismBearingHoldIdle -> {
                    viewModel.setRealismBearingHoldIdle(action.enabled)
                }

                is SettingsAction.SetRealismAltitudeEnabled -> {
                    viewModel.setRealismAltitudeEnabled(action.enabled)
                }

                is SettingsAction.SetRealismWarmupEnabled -> {
                    viewModel.setRealismWarmupEnabled(action.enabled)
                }

                is SettingsAction.SetRealismSatelliteExtrasEnabled -> {
                    viewModel.setRealismSatelliteExtrasEnabled(action.enabled)
                }

                is SettingsAction.SetRealismSuspendedMockingEnabled -> {
                    viewModel.setRealismSuspendedMockingEnabled(action.enabled)
                }

                is SettingsAction.SetJitterSpeedIdleVariationPct -> {
                    viewModel.setJitterSpeedIdleVariationPct(action.pct)
                }

                is SettingsAction.SetJitterSpeedMovingVariationPct -> {
                    viewModel.setJitterSpeedMovingVariationPct(action.pct)
                }

                is SettingsAction.SetHotLocationsEnabled -> {
                    viewModel.setHotLocationsEnabled(action.enabled)
                }

                is SettingsAction.SetSelectedHotLocationIds -> {
                    viewModel.setSelectedHotLocationIds(action.ids)
                }

                is SettingsAction.SetHotRoutesEnabled -> {
                    viewModel.setHotRoutesEnabled(action.enabled)
                }

                is SettingsAction.SetSelectedHotRouteIds -> {
                    viewModel.setSelectedHotRouteIds(action.ids)
                }

                is SettingsAction.UpdateRoamingDefaults -> {
                    viewModel.updateRoamingDefaults(action.defaults)
                }

                is SettingsAction.SetFloatingMapQuickWalk -> {
                    viewModel.setFloatingMapQuickWalk(action.enabled)
                }

                is SettingsAction.SetHideTeleportFeatures -> {
                    viewModel.setHideTeleportFeatures(action.enabled)
                }

                is SettingsAction.SetHideWidgetOverlay -> {
                    viewModel.setHideWidgetOverlay(action.enabled)
                }

                is SettingsAction.SetShowRouteJumpButtons -> {
                    viewModel.setShowRouteJumpButtons(action.enabled)
                }

                is SettingsAction.SetTapToWalkOverlayEnabled -> {
                    viewModel.setTapToWalkOverlayEnabled(action.enabled)
                }

                is SettingsAction.SetTapToWalkScaleMpx -> {
                    viewModel.setTapToWalkScaleMpx(action.scale)
                }

                is SettingsAction.SetCompassTrackingEnabled -> {
                    viewModel.setCompassTrackingEnabled(action.enabled)
                }

                is SettingsAction.SetCompassRegion -> {
                    viewModel.setCompassRegion(action.cx, action.cy, action.radius)
                }

                is SettingsAction.SetThemeMode -> {
                    viewModel.setThemeMode(action.mode)
                }

                SettingsAction.Export -> {
                    exportLauncher.launch(
                        "${AppConstants.ExportConstants.FILENAME_PREFIX}-${System.currentTimeMillis()}.json",
                    )
                }

                SettingsAction.Import -> {
                    importLauncher.launch(arrayOf(AppConstants.ExportConstants.MIME_TYPE))
                }

                SettingsAction.ImportGpsJoystick -> {
                    importGpsJoystickLauncher.launch(arrayOf("*/*"))
                }

                SettingsAction.ImportYamla -> {
                    importYamlaLauncher.launch(arrayOf("application/json"))
                }

                SettingsAction.QrShare -> {
                    viewModel.prepareQrExport()
                }

                SettingsAction.QrScan -> {
                    showQrScanner = true
                }

                SettingsAction.QrEnterCode -> {
                    showEnterCodeDialog = true
                }

                SettingsAction.SaveChanges -> {
                    viewModel.saveChanges()
                }

                SettingsAction.DiscardChanges -> {
                    viewModel.discardChanges()
                }

                is SettingsAction.SetAmapWebKey -> {
                    viewModel.setAmapWebKey(action.key)
                }

                SettingsAction.ResetAllData -> {
                    showResetConfirm = true
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = bottomBar,
    )
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    SettingsScreen(
        uiState = SettingsUiState(),
        onOpenDrawer = {},
        onAction = {},
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    roamingDefaults: RoamingDefaults = RoamingDefaults(),
    isRooted: Boolean = false,
    hotLocationTree: HotItemTree = HotItemTree.Empty,
    hotRouteTree: HotItemTree = HotItemTree.Empty,
    onOpenDrawer: () -> Unit = {},
    isSpoofing: Boolean = false,
    onToggleSpoofing: () -> Unit = {},
    locationLabel: String? = null,
    onAction: (SettingsAction) -> Unit,
    onCheckCompassService: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    amapKey: String = "",
) {
    var currentSection by remember { mutableStateOf<SettingsSection?>(null) }

    BackHandler(enabled = currentSection != null) {
        currentSection = null
    }

    when (currentSection) {
        null -> {
            SettingsHubScreen(
                uiState = uiState,
                onOpenDrawer = onOpenDrawer,
                onNavigate = { currentSection = it },
                isSpoofing = isSpoofing,
                onToggleSpoofing = onToggleSpoofing,
                locationLabel = locationLabel,
                onAction = onAction,
                bottomBar = bottomBar,
                snackbarHost = snackbarHost,
            )
        }

        SettingsSection.GPS -> {
            SettingsGpsSubScreen(
                uiState = uiState,
                amapKey = amapKey,
                onNavigateBack = { currentSection = null },
                isSpoofing = isSpoofing,
                onToggleSpoofing = onToggleSpoofing,
                locationLabel = locationLabel,
                onAction = onAction,
                bottomBar = bottomBar,
                snackbarHost = snackbarHost,
            )
        }

        SettingsSection.MENUS -> {
            SettingsMenusSubScreen(
                uiState = uiState,
                isRooted = isRooted,
                onNavigateBack = { currentSection = null },
                isSpoofing = isSpoofing,
                onToggleSpoofing = onToggleSpoofing,
                locationLabel = locationLabel,
                onAction = onAction,
                onCheckCompassService = onCheckCompassService,
                bottomBar = bottomBar,
                snackbarHost = snackbarHost,
            )
        }

        SettingsSection.FAVORITES_ROUTES -> {
            SettingsFavoritesRoutesSubScreen(
                uiState = uiState,
                hotLocationTree = hotLocationTree,
                hotRouteTree = hotRouteTree,
                onNavigateBack = { currentSection = null },
                isSpoofing = isSpoofing,
                onToggleSpoofing = onToggleSpoofing,
                locationLabel = locationLabel,
                onAction = onAction,
                bottomBar = bottomBar,
                snackbarHost = snackbarHost,
            )
        }

        SettingsSection.ROAMING -> {
            SettingsRoamingSubScreen(
                uiState = uiState,
                roamingDefaults = roamingDefaults,
                onNavigateBack = { currentSection = null },
                isSpoofing = isSpoofing,
                onToggleSpoofing = onToggleSpoofing,
                locationLabel = locationLabel,
                onAction = onAction,
                bottomBar = bottomBar,
                snackbarHost = snackbarHost,
            )
        }
    }
}

@Composable
private fun SettingsHubScreen(
    uiState: SettingsUiState,
    onOpenDrawer: () -> Unit,
    onNavigate: (SettingsSection) -> Unit,
    isSpoofing: Boolean,
    onToggleSpoofing: () -> Unit,
    locationLabel: String? = null,
    onAction: (SettingsAction) -> Unit,
    bottomBar: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    LjScaffold(
        title = "设置",
        isSpoofing = isSpoofing,
        onToggleSpoofing = onToggleSpoofing,
        locationLabel = locationLabel,
        onNavigationClick = onOpenDrawer,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        actions = {
            var showDownloadMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showDownloadMenu = true }) {
                    Icon(LjIcons.FileUpload, contentDescription = "导出")
                }
                DropdownMenu(expanded = showDownloadMenu, onDismissRequest = { showDownloadMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("通过二维码导出") },
                        onClick = {
                            showDownloadMenu = false
                            onAction(SettingsAction.QrShare)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("导出设置") },
                        onClick = {
                            showDownloadMenu = false
                            onAction(SettingsAction.Export)
                        },
                    )
                }
            }
            var showUploadMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showUploadMenu = true }) {
                    Icon(LjIcons.FileDownload, contentDescription = "导入")
                }
                DropdownMenu(expanded = showUploadMenu, onDismissRequest = { showUploadMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("从二维码导入") },
                        onClick = {
                            showUploadMenu = false
                            onAction(SettingsAction.QrScan)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("通过代码导入") },
                        onClick = {
                            showUploadMenu = false
                            onAction(SettingsAction.QrEnterCode)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("从文件导入") },
                        onClick = {
                            showUploadMenu = false
                            onAction(SettingsAction.Import)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("从 GPS Joystick 导入") },
                        onClick = {
                            showUploadMenu = false
                            onAction(SettingsAction.ImportGpsJoystick)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("从 YAMLA 导入") },
                        onClick = {
                            showUploadMenu = false
                            onAction(SettingsAction.ImportYamla)
                        },
                    )
                }
            }
            IconButton(onClick = { onAction(SettingsAction.ResetAllData) }) {
                Icon(
                    LjIcons.Delete,
                    contentDescription = "重置所有数据",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            if (uiState.isDirty) {
                TextButton(
                    onClick = { onAction(SettingsAction.DiscardChanges) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                ) { Text("放弃") }
                TextButton(
                    onClick = { onAction(SettingsAction.SaveChanges) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                ) { Text("保存") }
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(remember { ScrollState(0) })
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            AppIcon()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "定位模拟",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "v${AppConstants.AppInfo.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            SettingsDestinationCard(
                icon = LjIcons.Speed,
                title = "移动与 GPS",
                description = "所有移动模式的速度预设、信号真实度和位置随机性。",
                onClick = { onNavigate(SettingsSection.GPS) },
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsDestinationCard(
                icon = LjIcons.Joystick,
                title = "菜单",
                description = "控制悬浮小部件和地图按钮中显示的功能，以及如何通过点击触发行走。",
                onClick = { onNavigate(SettingsSection.MENUS) },
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsDestinationCard(
                icon = LjIcons.Favorite,
                title = "收藏与路线",
                description = "精选热门地点和预置路线，快速填充你的收藏库。",
                onClick = { onNavigate(SettingsSection.FAVORITES_ROUTES) },
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsDestinationCard(
                icon = LjIcons.Explore,
                title = "漫游",
                description = "随机漫步的默认区域、距离、速度和路线方式。",
                onClick = { onNavigate(SettingsSection.ROAMING) },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsDestinationCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun SubScreenActions(
    isDirty: Boolean,
    onAction: (SettingsAction) -> Unit,
) {
    if (isDirty) {
        TextButton(
            onClick = { onAction(SettingsAction.DiscardChanges) },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        ) { Text("放弃") }
        TextButton(
            onClick = { onAction(SettingsAction.SaveChanges) },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        ) { Text("保存") }
    }
}

@Composable
private fun ImportConfirmDialog(
    onReplace: () -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入数据") },
        text = { Text("如何处理现有数据？") },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("取消") }
                TextButton(onClick = onAdd) { Text("合并") }
                TextButton(onClick = onReplace) { Text("替换") }
            }
        },
        dismissButton = {},
    )
}

@Composable
private fun ResetAllDataConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重置所有数据？") },
        text = { Text("所有收藏、路线和设置都将被永久删除。此操作无法撤销。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("重置", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun EnterExportCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("输入导出代码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "向发送方询问其在二维码分享对话框中显示的 6 位代码。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase().take(6) },
                    label = { Text("代码") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (code.length == 6) onConfirm(code) }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(code) }, enabled = code.length == 6) { Text("导入") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
