package com.locationjoystick.feature.map.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.locationjoystick.core.data.CooldownState
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.component.CooldownAdvisoryBadge
import com.locationjoystick.core.designsystem.component.FavoritesList
import com.locationjoystick.core.designsystem.component.LjCheckboxRow
import com.locationjoystick.core.model.FavoriteLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutesPickerSheet(
    uiState: MapUiState,
    onAction: (MapAction) -> Unit,
) {
    var selectedRouteId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = { onAction(MapAction.CloseRoutesSheet) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = "路线", style = MaterialTheme.typography.headlineSmall)
            if (uiState.routes.isEmpty()) {
                Text(
                    text = "暂无保存的路线",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.routes, key = { it.id }) { route ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                                    .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = route.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "${route.waypoints.size} 个途经点",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Button(onClick = { selectedRouteId = route.id }) {
                                Icon(LjIcons.PlayArrow, contentDescription = "开始路线")
                            }
                        }
                    }
                }
            }
        }
    }

    selectedRouteId?.let { routeId ->
        StartRouteDialog(
            onDismiss = { selectedRouteId = null },
            onStart = { isLooping, isReverse, isReturnToLocation, teleportToStart ->
                onAction(MapAction.StartRouteReplay(routeId, isLooping, isReverse, isReturnToLocation, teleportToStart))
                selectedRouteId = null
            },
            hideTeleport = uiState.hideTeleportFeatures,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FavoritesPickerSheet(
    uiState: MapUiState,
    onAction: (MapAction) -> Unit,
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onAction(MapAction.CloseFavoritesPicker) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        val target = uiState.favoriteTarget
        if (target == null) {
            FavoritesList(
                title = "收藏",
                favorites = uiState.favorites,
                onSelect = { onAction(MapAction.SelectFavorite(it)) },
                onSaveCurrentLocation =
                    if (uiState.currentPosition != null) {
                        { showSaveDialog = true }
                    } else {
                        null
                    },
                cooldownLabel = { fav ->
                    (uiState.favoriteCooldownStates[fav.id] as? CooldownState.Cooling)?.toAdvisoryLabel()
                },
            )
        } else {
            FavoriteTargetDetail(
                favorite = target,
                onSetLocation = { onAction(MapAction.SetLocationTo(target.position)) },
                onGoToLocation = { onAction(MapAction.WalkStraightTo(target.position)) },
                onGoToLocationViaRoads = { onAction(MapAction.WalkViaRoadsTo(target.position)) },
                onDismiss = { onAction(MapAction.CloseFavoritesPicker) },
                hideTeleportFeatures = uiState.hideTeleportFeatures,
            )
        }
    }

    if (showSaveDialog) {
        SaveCurrentLocationDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                onAction(MapAction.SaveCurrentLocation(name))
                showSaveDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PendingTapSheet(
    position: com.locationjoystick.core.model.LatLng,
    isRouteReplay: Boolean,
    isWalkActive: Boolean,
    cooldownState: CooldownState,
    onAction: (MapAction) -> Unit,
    isEphemeralReplay: Boolean = false,
    onShare: (() -> Unit)? = null,
    hideTeleportFeatures: Boolean = false,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(MapAction.ClearPendingTap) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (isRouteReplay && !isEphemeralReplay) {
                Text("路线进行中", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                if (!hideTeleportFeatures) {
                    Button(
                        onClick = { onAction(MapAction.StopRouteAndTeleport(position)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("停止路线并传送")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = { onAction(MapAction.StopRouteAndWalkTo(position)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("停止路线并走到这里")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onAction(MapAction.FinishRouteAndWalkTo(position)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("结束路线并走到这里")
                }
            } else {
                Text("移动到该位置？", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                CooldownAdvisoryBadge(
                    (cooldownState as? CooldownState.Cooling)?.toAdvisoryLabel() ?: "无需等待",
                )
                Spacer(Modifier.height(16.dp))
                if (!hideTeleportFeatures) {
                    Button(
                        onClick = { onAction(MapAction.ConfirmTeleport(position)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("传送到这里")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = {
                        onAction(MapAction.LongPressTapToWalk(position))
                        onAction(MapAction.ClearPendingTap)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("走到这里")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        onAction(MapAction.WalkViaRoadsTo(position))
                        onAction(MapAction.ClearPendingTap)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("沿道路走到这里")
                }
                if (isWalkActive) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onAction(MapAction.AddEphemeralWaypoint(position)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("添加下一个点")
                    }
                }
            }
            if (onShare != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("分享此位置")
                }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = { onAction(MapAction.ClearPendingTap) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("关闭")
            }
        }
    }
}

@Composable
internal fun FavoriteTargetDetail(
    favorite: FavoriteLocation,
    onSetLocation: () -> Unit,
    onGoToLocation: () -> Unit,
    onGoToLocationViaRoads: () -> Unit,
    onDismiss: () -> Unit,
    hideTeleportFeatures: Boolean = false,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Text(favorite.name, style = MaterialTheme.typography.headlineSmall)
        Text(
            "${String.format("%.4f", favorite.position.latitude)}, " +
                "${String.format("%.4f", favorite.position.longitude)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )

        if (!hideTeleportFeatures) {
            Button(
                onClick = onSetLocation,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
            ) {
                Text("设置位置")
            }
        }
        OutlinedButton(
            onClick = onGoToLocation,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
        ) {
            Text("走到该位置")
        }
        OutlinedButton(
            onClick = onGoToLocationViaRoads,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
        ) {
            Text("沿道路行走")
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text("不做任何操作")
        }
    }
}

@Composable
internal fun SaveCurrentLocationDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存当前位置") },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) onSave(name.trim())
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun StartRouteDialog(
    onDismiss: () -> Unit,
    onStart: (isLooping: Boolean, isReverse: Boolean, isReturnToLocation: Boolean, teleportToStart: Boolean) -> Unit,
    hideTeleport: Boolean = false,
) {
    var loop by remember { mutableStateOf(false) }
    var reverse by remember { mutableStateOf(false) }
    var returnToLocation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("开始路线") },
        text = {
            Column {
                LjCheckboxRow(title = "循环", checked = loop, enabled = !returnToLocation, onCheckedChange = { loop = it })
                LjCheckboxRow(title = "反向", checked = reverse, onCheckedChange = { reverse = it })
                LjCheckboxRow(
                    title = "返回原位置",
                    checked = returnToLocation,
                    enabled = !loop,
                    onCheckedChange = { returnToLocation = it },
                )
                if (!hideTeleport) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onStart(loop, reverse, returnToLocation && !loop, true) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("传送并开始")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onStart(loop, reverse, returnToLocation && !loop, false) }) {
                Text("行走并开始")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
