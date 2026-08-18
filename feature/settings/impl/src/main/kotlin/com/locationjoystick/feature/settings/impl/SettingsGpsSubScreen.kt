package com.locationjoystick.feature.settings.impl

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.component.LjCheckboxRow
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.designsystem.component.LjSegmentedControl
import com.locationjoystick.core.model.SpeedProfile
import com.locationjoystick.core.model.SpeedUnit

@Composable
internal fun SettingsGpsSubScreen(
    uiState: SettingsUiState,
    amapKey: String = "",
    onNavigateBack: () -> Unit,
    isSpoofing: Boolean,
    onToggleSpoofing: () -> Unit,
    locationLabel: String? = null,
    onAction: (SettingsAction) -> Unit,
    bottomBar: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    LjScaffold(
        title = "移动与 GPS",
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
                    val isMph = uiState.speedUnit == SpeedUnit.MPH
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(remember { ScrollState(0) })
                                .padding(16.dp),
                    ) {
                        SpeedProfilesSection(uiState, onAction)
                        Spacer(modifier = Modifier.height(24.dp))
                        GpsJitterSection(uiState, isMph, onAction)
                        Spacer(modifier = Modifier.height(24.dp))
                        GpsRealismSection(uiState, onAction)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("位置记忆", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "控制应用重新打开时是否记住上次的位置。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LjCheckboxRow(
                            checked = uiState.rememberLastLocation,
                            onCheckedChange = { onAction(SettingsAction.SetRememberLastLocation(it)) },
                            title = "记住上次位置",
                            description = "应用重启时恢复你上次设置的位置。",
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("高德地图 API Key", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "用于地图搜索（地理编码）。填入高德「Web 服务」key 后即可使用中文搜索。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        var showAmapHint by remember { mutableStateOf(false) }
                        TextButton(onClick = { showAmapHint = !showAmapHint }) {
                            Text(if (showAmapHint) "收起申请步骤" else "如何申请 key？")
                        }
                        if (showAmapHint) {
                            Text(
                                "① 打开 lbs.amap.com 注册登录\n② 控制台「应用管理」→ 创建应用\n③ 添加「Web 服务」类型的 Key\n④ 复制 Key 填入下方，点右上角「保存」",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        var keyInput by remember { mutableStateOf(amapKey) }
                        LaunchedEffect(amapKey) { keyInput = amapKey }
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = {
                                keyInput = it
                                onAction(SettingsAction.SetAmapWebKey(it))
                            },
                            label = { Text("高德 Web 服务 Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedProfilesSection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Text("速度方案", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "摇杆、路线回放和漫游使用的移动速度。先选择单位，再设置每个预设。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("单位", modifier = Modifier.weight(0.3f))
        LjSegmentedControl(
            options = listOf(SpeedUnit.KMH to "km/h", SpeedUnit.MPH to "mph"),
            selected = uiState.speedUnit,
            onSelect = { onAction(SettingsAction.SetSpeedUnit(it)) },
            modifier = Modifier.weight(0.7f),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    val profiles = SpeedProfile.defaultProfiles()
    profiles.forEachIndexed { index, profile ->
        val speedMs = uiState.speeds.getValue(profile.id)
        SpeedProfileInput(
            label = profile.name,
            displaySpeed = convertMsToDisplay(speedMs, uiState.speedUnit),
            onSpeedChange = { onAction(SettingsAction.SetSpeed(profile.id, it)) },
            unit = if (uiState.speedUnit == SpeedUnit.KMH) "km/h" else "mph",
        )
        if (speedMs > AppConstants.ProfileConstants.ANTI_CHEAT_WARNING_THRESHOLD_MS) AntiCheatWarning()
        if (index != profiles.lastIndex) Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SpeedProfileInput(
    label: String,
    displaySpeed: Double,
    onSpeedChange: (Double) -> Unit,
    unit: String,
) {
    var localValue by remember { mutableStateOf(String.format("%.1f", displaySpeed)) }
    var lastSentValue by remember { mutableStateOf(displaySpeed) }

    LaunchedEffect(displaySpeed) {
        if (displaySpeed != lastSentValue) {
            localValue = String.format("%.1f", displaySpeed)
            lastSentValue = displaySpeed
        }
    }

    val parsedValue = localValue.toDoubleOrNull()
    val isValid = parsedValue != null && parsedValue > 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(0.2f))

        Row(
            modifier = Modifier.weight(0.8f).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = localValue,
                onValueChange = { newValue ->
                    localValue = newValue
                    val parsed = newValue.toDoubleOrNull()
                    if (parsed != null && parsed > 0.0) {
                        onSpeedChange(parsed)
                        lastSentValue = parsed
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = localValue.isNotBlank() && !isValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(unit, modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
private fun AntiCheatWarning() {
    Text(
        text = "速度超过 8 米/秒——可能在某些游戏中触发反作弊检测",
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
    )
}

private fun convertMsToDisplay(
    ms: Double,
    unit: SpeedUnit,
): Double =
    when (unit) {
        SpeedUnit.KMH -> ms * 3.6
        SpeedUnit.MPH -> ms * 2.237
    }
