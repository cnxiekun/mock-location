package com.locationjoystick.feature.group.impl

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locationjoystick.core.data.CooldownState
import com.locationjoystick.core.data.toBadgeText
import com.locationjoystick.core.designsystem.component.CooldownAdvisoryBadge
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.location.rememberSpoofToggleState
import com.locationjoystick.core.model.GroupRole
import com.locationjoystick.core.model.GroupState
import com.locationjoystick.core.model.LatLng

@Composable
fun GroupSyncRoute(
    onOpenDrawer: () -> Unit,
    viewModel: GroupSyncViewModel = hiltViewModel(),
) {
    val groupState by viewModel.groupState.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val isDiscovering by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val followerCount by viewModel.followerCount.collectAsStateWithLifecycle()
    val hideTeleportFeatures by viewModel.hideTeleportFeatures.collectAsStateWithLifecycle()
    val leaderPosition by viewModel.leaderPosition.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val cooldownState by viewModel.cooldownState.collectAsStateWithLifecycle()
    val spoofToggle = rememberSpoofToggleState()

    var showQrScanner by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }

    LaunchedEffect(groupState.role) {
        if (groupState.role != GroupRole.NONE) showQrScanner = false
    }

    if (showQrScanner) {
        GroupQrScannerScreen(
            onQrScanned = { url ->
                viewModel.joinViaScannedUrl(url)
            },
            onNavigateBack = { showQrScanner = false },
        )
    } else {
        GroupSyncScreen(
            groupState = groupState,
            qrBitmap = qrBitmap,
            snackbarHostState = snackbarHostState,
            isDiscovering = isDiscovering,
            followerCount = followerCount,
            isSpoofing = spoofToggle.isSpoofing,
            onToggleSpoofing = spoofToggle.onToggle,
            locationLabel = spoofToggle.locationLabel,
            onOpenDrawer = onOpenDrawer,
            onCreateGroup = viewModel::createGroup,
            onJoinViaQr = { showQrScanner = true },
            onJoinByCode = viewModel::joinByCode,
            onSetFollowerModeEnabled = viewModel::setFollowerModeEnabled,
            onSetSharingEnabled = viewModel::setSharingEnabled,
            onTeleportToLeaderNow = viewModel::teleportToLeaderNow,
            onLeaveGroup = viewModel::leaveGroup,
            onRegenerateQr = viewModel::regenerateQr,
            hideTeleportFeatures = hideTeleportFeatures,
            leaderPosition = leaderPosition,
            currentPosition = currentPosition,
            cooldownState = cooldownState,
        )
    }
}

@Composable
internal fun GroupSyncScreen(
    groupState: GroupState,
    qrBitmap: Bitmap?,
    snackbarHostState: SnackbarHostState,
    isDiscovering: Boolean,
    followerCount: Int,
    isSpoofing: Boolean,
    onToggleSpoofing: () -> Unit,
    locationLabel: String? = null,
    onOpenDrawer: () -> Unit,
    onCreateGroup: () -> Unit,
    onJoinViaQr: () -> Unit,
    onJoinByCode: (String) -> Unit,
    onSetFollowerModeEnabled: (Boolean) -> Unit,
    onSetSharingEnabled: (Boolean) -> Unit,
    onTeleportToLeaderNow: () -> Unit,
    onLeaveGroup: () -> Unit,
    onRegenerateQr: () -> Unit,
    hideTeleportFeatures: Boolean = false,
    leaderPosition: LatLng? = null,
    currentPosition: LatLng? = null,
    cooldownState: CooldownState = CooldownState.Ready,
) {
    LjScaffold(
        title = "群组同步",
        isSpoofing = isSpoofing,
        onToggleSpoofing = onToggleSpoofing,
        locationLabel = locationLabel,
        onNavigationClick = onOpenDrawer,
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            when (groupState.role) {
                GroupRole.NONE -> {
                    NoGroupContent(
                        isDiscovering = isDiscovering,
                        onCreateGroup = onCreateGroup,
                        onJoinViaQr = onJoinViaQr,
                        onJoinByCode = onJoinByCode,
                    )
                }

                GroupRole.LEADER -> {
                    LeaderContent(
                        groupState = groupState,
                        qrBitmap = qrBitmap,
                        followerCount = followerCount,
                        onSetSharingEnabled = onSetSharingEnabled,
                        onLeaveGroup = onLeaveGroup,
                        onRegenerateQr = onRegenerateQr,
                    )
                }

                GroupRole.FOLLOWER -> {
                    FollowerContent(
                        groupState = groupState,
                        followerCount = followerCount,
                        onSetFollowerModeEnabled = onSetFollowerModeEnabled,
                        onTeleportToLeaderNow = onTeleportToLeaderNow,
                        onLeaveGroup = onLeaveGroup,
                        hideTeleportFeatures = hideTeleportFeatures,
                        leaderPosition = leaderPosition,
                        currentPosition = currentPosition,
                        cooldownState = cooldownState,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NoGroupContent(
    isDiscovering: Boolean,
    onCreateGroup: () -> Unit,
    onJoinViaQr: () -> Unit,
    onJoinByCode: (String) -> Unit,
) {
    var showCodeDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Rounded.Groups,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "群组同步",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "在同一 Wi-Fi 网络的多台设备之间同步你的模拟位置。无需账号。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))

        FilledTonalButton(
            onClick = onCreateGroup,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("创建群组——我是队长")
        }

        Text(
            text = "——或加入已有群组——",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onJoinViaQr,
                modifier = Modifier.weight(1f),
                enabled = !isDiscovering,
            ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("扫码")
            }
            OutlinedButton(
                onClick = { showCodeDialog = true },
                modifier = Modifier.weight(1f),
                enabled = !isDiscovering,
            ) {
                if (isDiscovering) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("输入代码")
                }
            }
        }
    }

    if (showCodeDialog) {
        EnterCodeDialog(
            onDismiss = { showCodeDialog = false },
            onConfirm = { code ->
                showCodeDialog = false
                onJoinByCode(code)
            },
        )
    }
}

@Composable
private fun EnterCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("输入群组代码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "向队长获取他们的 6 位群组代码。",
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
            TextButton(
                onClick = { onConfirm(code) },
                enabled = code.length == 6,
            ) {
                Text("加入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun LeaderContent(
    groupState: GroupState,
    qrBitmap: Bitmap?,
    followerCount: Int,
    onSetSharingEnabled: (Boolean) -> Unit,
    onLeaveGroup: () -> Unit,
    onRegenerateQr: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "角色：队长",
            style = MaterialTheme.typography.titleMedium,
        )

        val code = groupState.groupId ?: "—"
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
            ) {
                Text(
                    text = "群组代码",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = code,
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "跟随者可以扫码或输入此代码",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (followerCount == 1) "已连接 1 位跟随者" else "已连接 $followerCount 位跟随者",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        if (qrBitmap != null) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "群组邀请二维码",
                        modifier = Modifier.size(200.dp),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = " 扫码加入",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onRegenerateQr) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "重新生成二维码")
                        }
                    }
                }
            }
        }

        SwitchRow(
            label = "共享",
            description = "将你的位置发送给跟随者",
            checked = groupState.sharingEnabled,
            onCheckedChange = onSetSharingEnabled,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLeaveGroup,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Text("离开群组")
        }
    }
}

@Composable
private fun FollowerContent(
    groupState: GroupState,
    followerCount: Int,
    onSetFollowerModeEnabled: (Boolean) -> Unit,
    onTeleportToLeaderNow: () -> Unit,
    onLeaveGroup: () -> Unit,
    hideTeleportFeatures: Boolean = false,
    leaderPosition: LatLng? = null,
    currentPosition: LatLng? = null,
    cooldownState: CooldownState = CooldownState.Ready,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "角色：跟随者",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = "已连接到群组 ${groupState.groupId ?: "—"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (followerCount > 0) {
            Text(
                text = if (followerCount == 1) "本群组有 1 位跟随者" else "本群组有 $followerCount 位跟随者",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SwitchRow(
            label = "跟随队长",
            description = "朝队长的位置行走",
            checked = groupState.followerModeEnabled,
            onCheckedChange = onSetFollowerModeEnabled,
        )

        Text(
            text = "重启后打开应用时，跟随模式会自动恢复。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (groupState.followerModeEnabled && !hideTeleportFeatures) {
            leaderPosition?.let { leaderPos ->
                CooldownAdvisoryBadge(cooldownState.toBadgeText(currentPosition, leaderPos))
            }
            OutlinedButton(
                onClick = onTeleportToLeaderNow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("立即传送到队长位置")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLeaveGroup,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Text("离开群组")
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
