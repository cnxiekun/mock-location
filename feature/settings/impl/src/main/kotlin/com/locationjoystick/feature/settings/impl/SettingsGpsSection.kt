package com.locationjoystick.feature.settings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.locationjoystick.core.designsystem.component.LjCheckboxRow
import kotlin.math.roundToInt

private fun formatJitterDouble(d: Double): String {
    val rounded = (d * 100).roundToInt() / 100.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

@Composable
private fun JitterInput(
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
) {
    var localValue by remember { mutableStateOf(formatJitterDouble(value)) }
    var lastSentValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != lastSentValue) {
            localValue = formatJitterDouble(value)
            lastSentValue = value
        }
    }

    OutlinedTextField(
        value = localValue,
        onValueChange = { v ->
            localValue = v
            v.toDoubleOrNull()?.let {
                onValueChange(it)
                lastSentValue = it
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
private fun JitterInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
) {
    var localValue by remember { mutableStateOf(value.toString()) }
    var lastSentValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != lastSentValue) {
            localValue = value.toString()
            lastSentValue = value
        }
    }

    OutlinedTextField(
        value = localValue,
        onValueChange = { v ->
            localValue = v
            v.toIntOrNull()?.let {
                onValueChange(it)
                lastSentValue = it
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
internal fun GpsJitterSection(
    uiState: SettingsUiState,
    isMph: Boolean,
    onAction: (SettingsAction) -> Unit,
) {
    Text("位置随机性", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "为你的模拟位置添加小幅随机偏移，使其看起来更自然。设为 0 可禁用。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text("位置抖动", style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        JitterInput(
            value = if (isMph) uiState.jitterIdleRadiusMeters * 3.28084 else uiState.jitterIdleRadiusMeters,
            onValueChange = { onAction(SettingsAction.SetJitterIdleRadius(if (isMph) it / 3.28084 else it)) },
            label = if (isMph) "静止时抖动（英尺）" else "静止时抖动（米）",
            modifier = Modifier.weight(1f),
        )
        JitterInput(
            value = uiState.jitterIdleIntervalSeconds,
            onValueChange = { onAction(SettingsAction.SetJitterIdleIntervalSeconds(it)) },
            label = "静止时频率（秒）",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        JitterInput(
            value = if (isMph) uiState.jitterMovingRadiusMeters * 3.28084 else uiState.jitterMovingRadiusMeters,
            onValueChange = { onAction(SettingsAction.SetJitterMovingRadius(if (isMph) it / 3.28084 else it)) },
            label = if (isMph) "移动时抖动（英尺）" else "移动时抖动（米）",
            modifier = Modifier.weight(1f),
        )
        JitterInput(
            value = uiState.jitterIntervalSeconds,
            onValueChange = { onAction(SettingsAction.SetJitterIntervalSeconds(it)) },
            label = "移动时频率（秒）",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text("速度变化", style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        JitterInput(
            value = uiState.jitterSpeedIdleVariationPct.toDouble(),
            onValueChange = { onAction(SettingsAction.SetJitterSpeedIdleVariationPct(it.toInt())) },
            label = "静止时速度波动（%）",
            modifier = Modifier.weight(1f),
        )
        JitterInput(
            value = uiState.jitterSpeedMovingVariationPct.toDouble(),
            onValueChange = { onAction(SettingsAction.SetJitterSpeedMovingVariationPct(it.toInt())) },
            label = "移动时速度波动（%）",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun GpsRealismSection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Text("GPS 真实度", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "控制模拟 GPS 信号的行为。这些选项会添加真实 GPS 芯片产生的元数据和变化——" +
            "一些应用和游戏会检查这些信号来检测模拟位置。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
    LjCheckboxRow(
        checked = uiState.realismBearingHoldIdle,
        onCheckedChange = { onAction(SettingsAction.SetRealismBearingHoldIdle(it)) },
        title = "静止时保持方向",
        description =
            "停止移动时保持最后已知的方向，而不是瞬间回到 0°（正北）。" +
                "真实 GPS 芯片也是如此——突然重置到正北是常见的模拟定位破绽。",
    )
    LjCheckboxRow(
        checked = uiState.realismAltitudeEnabled,
        onCheckedChange = { onAction(SettingsAction.SetRealismAltitudeEnabled(it)) },
        title = "改变海拔",
        description =
            "模拟合理的海拔高度，带小幅随机漂移，而不是始终报告 0 米。" +
                "持续为 0 的海拔是模拟位置的明显信号。",
    )
    LjCheckboxRow(
        checked = uiState.realismWarmupEnabled,
        onCheckedChange = { onAction(SettingsAction.SetRealismWarmupEnabled(it)) },
        title = "GPS 预热模拟",
        description =
            "每次会话开始时报告略有偏差的读数，并在约 30 秒内逐渐改善，" +
                "就像真实 GPS 需要时间锁定信号一样。" +
                "默认关闭，因为它在会话开始时会暂时降低定位精度。",
    )
    LjCheckboxRow(
        checked = uiState.realismSatelliteExtrasEnabled,
        onCheckedChange = { onAction(SettingsAction.SetRealismSatelliteExtrasEnabled(it)) },
        title = "真实的卫星数量",
        description =
            "为每次更新附加卫星元数据（可见卫星 7–14 颗，锁定 6–12 颗），而不是 0。" +
                "一些应用会通过检查卫星数为 0 来识别模拟定位。",
    )
    LjCheckboxRow(
        checked = uiState.realismSuspendedMockingEnabled,
        onCheckedChange = { onAction(SettingsAction.SetRealismSuspendedMockingEnabled(it)) },
        title = "模拟信号中断",
        description =
            "大约每 10 秒短暂暂停模拟位置信号，就像真实 GPS 暂时丢失信号一样。" +
                "默认关闭——暂停会导致大多数应用出现明显的卡顿。" +
                "路线回放期间会自动跳过。",
    )
}
