package com.locationjoystick.feature.routes.impl

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.UiConstants
import com.locationjoystick.core.designsystem.component.FavoritesList
import com.locationjoystick.core.designsystem.component.LjMapIconButton
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.designsystem.component.NominatimSearchBar
import com.locationjoystick.core.location.rememberSpoofToggleState
import com.locationjoystick.core.map.geojson.buildPositionGeoJson
import com.locationjoystick.core.map.geojson.buildSegmentsGeoJson
import com.locationjoystick.core.map.geojson.buildWaypointsGeoJson
import com.locationjoystick.core.common.util.Gcj02Converter
import com.locationjoystick.core.map.maplibre.addCreatorLayers
import com.locationjoystick.core.model.FavoriteLocation
import com.locationjoystick.core.model.LatLng
import com.locationjoystick.core.model.RecentSearch
import com.locationjoystick.core.overlay.OverlayService
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.geometry.LatLng as MapLatLng

@Composable
fun RouteCreatorRoute(
    onRouteSaved: () -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    val viewModel: RouteCreatorViewModel = hiltViewModel()
    val spoofToggle = rememberSpoofToggleState()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val livePosition by viewModel.livePosition.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val amapKey by viewModel.amapKey.collectAsStateWithLifecycle()

    RouteCreatorScreen(
        state = state,
        initialPosition = viewModel.currentPosition,
        favorites = favorites,
        currentPosition = livePosition,
        recentSearches = recentSearches,
        amapKey = amapKey,
        onAddWaypoint = viewModel::addWaypoint,
        onUndo = viewModel::undoLastWaypoint,
        onSaveRoute = { name ->
            viewModel.saveRoute(name)
            onRouteSaved()
        },
        onSearchCommitted = viewModel::addRecentSearch,
        onBack = onBack,
        isSpoofing = spoofToggle.isSpoofing,
        onToggleSpoofing = spoofToggle.onToggle,
        locationLabel = spoofToggle.locationLabel,
        bottomBar = bottomBar,
    )
}

@Preview(showBackground = true)
@Composable
private fun RouteCreatorScreenPreview() {
    RouteCreatorScreen(
        state = CreatorState(),
        initialPosition = null,
        onAddWaypoint = {},
        onUndo = {},
        onSaveRoute = {},
        onBack = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteCreatorScreen(
    state: CreatorState,
    initialPosition: LatLng? = null,
    favorites: List<FavoriteLocation> = emptyList(),
    currentPosition: LatLng? = null,
    recentSearches: List<RecentSearch> = emptyList(),
    amapKey: String = "",
    onAddWaypoint: (LatLng) -> Unit,
    onUndo: () -> Unit,
    onSaveRoute: (String) -> Unit,
    onSearchCommitted: ((String, Double, Double) -> Unit)? = null,
    onBack: () -> Unit,
    isSpoofing: Boolean = false,
    onToggleSpoofing: () -> Unit = {},
    locationLabel: String? = null,
    bottomBar: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView =
        remember {
            MapLibre.getInstance(context)
            MapView(context)
        }
    val mapRef = remember { mutableStateOf<MapLibreMap?>(null) }
    val segmentsSource = remember { mutableStateOf<GeoJsonSource?>(null) }
    val waypointsSource = remember { mutableStateOf<GeoJsonSource?>(null) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showFavoritesSheet by remember { mutableStateOf(false) }

    LaunchedEffect(showSaveDialog) {
        context.sendBroadcast(
            Intent(
                if (showSaveDialog) {
                    OverlayService.ACTION_OVERLAY_HIDE
                } else {
                    OverlayService.ACTION_OVERLAY_SHOW
                },
            ),
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    LjScaffold(
        title = "创建路线",
        isSpoofing = isSpoofing,
        onToggleSpoofing = onToggleSpoofing,
        locationLabel = locationLabel,
        onNavigationClick = onBack,
        navigationIcon = LjIcons.ArrowBack,
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = bottomBar,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(UiConstants.FAB_SPACING),
            ) {
                LjMapIconButton(
                    icon = LjIcons.Search,
                    contentDescription = "搜索位置",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { showSearch = !showSearch },
                )
                if (currentPosition != null) {
                    LjMapIconButton(
                        icon = LjIcons.MyLocation,
                        contentDescription = "居中到当前位置",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = {
                            currentPosition?.let { pos ->
                                val display = Gcj02Converter.wgs84ToGcj02(pos.latitude, pos.longitude)
                                mapRef.value?.animateCamera(
                                    CameraUpdateFactory.newLatLng(
                                        MapLatLng(display.latitude, display.longitude),
                                    ),
                                    500,
                                )
                            }
                        },
                    )
                }
                LjMapIconButton(
                    icon = LjIcons.Favorite,
                    contentDescription = "从收藏夹选择",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = { showFavoritesSheet = true },
                )
                LjMapIconButton(
                    icon = LjIcons.Undo,
                    contentDescription = "撤销最后一个途经点",
                    containerColor =
                        if (state.waypoints.isNotEmpty()) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    contentColor =
                        if (state.waypoints.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    onClick = onUndo,
                )
                if (state.waypoints.size >= 2) {
                    LjMapIconButton(
                        icon = LjIcons.Save,
                        contentDescription = "保存路线",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = { showSaveDialog = true },
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            AndroidView(
                factory = { ctx ->
                    MapLibre.getInstance(ctx)
                    mapView.apply {
                        getMapAsync { map ->
                            mapRef.value = map
                            map.uiSettings.isAttributionEnabled = false
                            map.uiSettings.isLogoEnabled = false
                            val initialDisplay =
                                initialPosition?.let {
                                    Gcj02Converter.wgs84ToGcj02(it.latitude, it.longitude)
                                } ?: Gcj02Converter.wgs84ToGcj02(
                                    AppConstants.MapConstants.DEFAULT_LAT,
                                    AppConstants.MapConstants.DEFAULT_LON,
                                )
                            map.cameraPosition =
                                CameraPosition
                                    .Builder()
                                    .target(MapLatLng(initialDisplay.latitude, initialDisplay.longitude))
                                    .zoom(AppConstants.MapConstants.DEFAULT_ZOOM)
                                    .build()

                            map.setStyle(Style.Builder().fromUri(AppConstants.MapConstants.EMPTY_MAP_STYLE_URI)) { style ->
                                val layers =
                                    style.addCreatorLayers(
                                        currentPosGeoJson =
                                            initialPosition?.let {
                                                val display = Gcj02Converter.wgs84ToGcj02(it.latitude, it.longitude)
                                                buildPositionGeoJson(display)
                                            },
                                    )
                                segmentsSource.value = layers.segmentsSource
                                waypointsSource.value = layers.waypointsSource
                            }

                            map.addOnMapClickListener { latLng ->
                                // 底图为 GCJ-02，转回 WGS-84 存储
                                val wgs = Gcj02Converter.gcj02ToWgs84(latLng.latitude, latLng.longitude)
                                onAddWaypoint(LatLng(wgs.latitude, wgs.longitude))
                                true
                            }
                        }
                    }
                },
                update = { _ ->
                    val segSrc = segmentsSource.value ?: return@AndroidView
                    val wpSrc = waypointsSource.value ?: return@AndroidView

                    val displaySegments = state.segments.map { seg -> seg.map { Gcj02Converter.wgs84ToGcj02(it.latitude, it.longitude) } }
                    val displayWaypoints = state.waypoints.map { Gcj02Converter.wgs84ToGcj02(it.latitude, it.longitude) }
                    segSrc.setGeoJson(buildSegmentsGeoJson(displaySegments))
                    wpSrc.setGeoJson(buildWaypointsGeoJson(displayWaypoints))
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (state.isLoadingSegment) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Search bar — top overlay, shown when toggled
            if (showSearch) {
                NominatimSearchBar(
                    onLocationSelected = { lat, lon, _ ->
                        onAddWaypoint(LatLng(lat, lon))
                        showSearch = false
                        val display = Gcj02Converter.wgs84ToGcj02(lat, lon)
                        val map = mapRef.value ?: return@NominatimSearchBar
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                MapLatLng(display.latitude, display.longitude),
                                AppConstants.MapConstants.DEFAULT_ZOOM,
                            ),
                            500,
                        )
                    },
                    recentSearches = recentSearches,
                    onSearchCommitted = onSearchCommitted,
                    amapKey = amapKey,
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(
                                top = paddingValues.calculateTopPadding() + 8.dp,
                                start = 12.dp,
                                end = 12.dp,
                            ),
                )
            }
        }
    }

    if (showSaveDialog) {
        SaveRouteDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                onSaveRoute(name)
                showSaveDialog = false
            },
        )
    }

    if (showFavoritesSheet) {
        CreatorFavoritesSheet(
            favorites = favorites,
            onSelect = { position ->
                showFavoritesSheet = false
                val display = Gcj02Converter.wgs84ToGcj02(position.latitude, position.longitude)
                mapRef.value?.animateCamera(
                    CameraUpdateFactory.newLatLng(MapLatLng(display.latitude, display.longitude)),
                    500,
                )
            },
            onDismiss = { showFavoritesSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorFavoritesSheet(
    favorites: List<FavoriteLocation>,
    onSelect: (LatLng) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        FavoritesList(
            title = "跳转到收藏",
            favorites = favorites,
            onSelect = { onSelect(it.position) },
        )
    }
}

@Composable
private fun SaveRouteDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存路线") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("路线名称") },
                modifier = Modifier,
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim())
                    }
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
