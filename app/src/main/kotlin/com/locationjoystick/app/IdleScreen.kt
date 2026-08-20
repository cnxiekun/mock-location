package com.locationjoystick.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.component.AppIcon
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.location.rememberSpoofToggleState

internal const val IDLE_ROUTE = "idle"

@Composable
internal fun IdleScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGroup: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
) {
    val isWide = LocalConfiguration.current.screenWidthDp >= 600
    val spoofToggle = rememberSpoofToggleState()

    LjScaffold(
        title = "主页",
        isSpoofing = spoofToggle.isSpoofing,
        onToggleSpoofing = spoofToggle.onToggle,
        locationLabel = spoofToggle.locationLabel,
        onNavigationClick = onOpenDrawer,
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        if (isWide) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 32.dp),
                    ) {
                        AppIcon()
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "模拟定位",
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
                    }
                }
                item {
                    IdleDestinationCard(
                        LjIcons.Map,
                        "地图",
                        "模拟你的 GPS 位置并在地图上控制移动。",
                        onNavigateToMap,
                    )
                }
                item { IdleDestinationCard(LjIcons.Route, "路线", "回放已保存的路线。", onNavigateToRoutes) }
                item {
                    IdleDestinationCard(
                        LjIcons.Favorite,
                        "收藏",
                        "传送到或步行至已保存的位置。",
                        onNavigateToFavorites,
                    )
                }
                item {
                    IdleDestinationCard(
                        LjIcons.Share,
                        "群组同步",
                        "将你的位置同步到同一 Wi-Fi 下的其他设备。",
                        onNavigateToGroup,
                    )
                }
                item {
                    IdleDestinationCard(
                        LjIcons.Settings,
                        "设置",
                        "配置模拟定位的偏好设置。",
                        onNavigateToSettings,
                    )
                }
            }
        } else {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                AppIcon()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "模拟定位",
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

                IdleDestinationCard(
                    icon = LjIcons.Map,
                    title = "地图",
                    description = "模拟你的 GPS 位置并在地图上控制移动。",
                    onClick = onNavigateToMap,
                )
                Spacer(modifier = Modifier.height(12.dp))
                IdleDestinationCard(
                    icon = LjIcons.Route,
                    title = "路线",
                    description = "回放已保存的路线。",
                    onClick = onNavigateToRoutes,
                )
                Spacer(modifier = Modifier.height(12.dp))
                IdleDestinationCard(
                    icon = LjIcons.Favorite,
                    title = "收藏",
                    description = "传送到或步行至已保存的位置。",
                    onClick = onNavigateToFavorites,
                )
                Spacer(modifier = Modifier.height(12.dp))
                IdleDestinationCard(
                    icon = LjIcons.Share,
                    title = "群组同步",
                    description = "将你的位置同步到同一 Wi-Fi 下的其他设备。",
                    onClick = onNavigateToGroup,
                )
                Spacer(modifier = Modifier.height(12.dp))
                IdleDestinationCard(
                    icon = LjIcons.Settings,
                    title = "设置",
                    description = "配置模拟定位的偏好设置。",
                    onClick = onNavigateToSettings,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun IdleDestinationCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
