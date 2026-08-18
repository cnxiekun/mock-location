package com.locationjoystick.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.locationjoystick.app.navigation.LjDrawerContent
import com.locationjoystick.app.navigation.LjNavHost
import com.locationjoystick.core.model.RouteType
import com.locationjoystick.feature.favorites.api.FAVORITES_ROUTE
import com.locationjoystick.feature.map.api.MAP_ROUTE
import com.locationjoystick.feature.routes.api.ROUTES_ROUTE
import com.locationjoystick.feature.routes.api.ROUTE_CREATOR_ROUTE
import com.locationjoystick.feature.settings.api.SETTINGS_ROUTE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
fun LjApp(
    navigateToMapFlow: Flow<Unit> = emptyFlow(),
    navigateToRouteCreatorFlow: Flow<Unit> = emptyFlow(),
    navigateToFavoritesFlow: Flow<Unit> = emptyFlow(),
    navigateToRoutesFlow: Flow<Unit> = emptyFlow(),
    deepLinkFailedFlow: Flow<Unit> = emptyFlow(),
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        deepLinkFailedFlow.collect {
            snackbarHostState.showSnackbar("无法打开该链接")
        }
    }

    LaunchedEffect(navController) {
        navigateToMapFlow.collect {
            drawerState.close()
            navController.navigate(MAP_ROUTE) {
                popUpTo(MAP_ROUTE) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(navController) {
        navigateToRouteCreatorFlow.collect {
            drawerState.close()
            navController.navigate("$ROUTE_CREATOR_ROUTE/${RouteType.STRAIGHT.name}") {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(navController) {
        launch {
            navigateToFavoritesFlow.collect {
                drawerState.close()
                navController.navigate(FAVORITES_ROUTE) {
                    launchSingleTop = true
                    popUpTo(IDLE_ROUTE) { saveState = true }
                    restoreState = true
                }
            }
        }
        launch {
            navigateToRoutesFlow.collect {
                drawerState.close()
                navController.navigate(ROUTES_ROUTE) {
                    launchSingleTop = true
                    popUpTo(IDLE_ROUTE) { saveState = true }
                    restoreState = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = false,
            drawerContent = {
                LjDrawerContent(
                    navController = navController,
                    drawerState = drawerState,
                )
            },
        ) {
            LjNavHost(
                navController = navController,
                onOpenDrawer = { scope.launch { drawerState.open() } },
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
