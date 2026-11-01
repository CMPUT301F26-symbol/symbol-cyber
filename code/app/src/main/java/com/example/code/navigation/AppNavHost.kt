/*
 * AppNavHost.kt
 * Navigation shell: top bar, bottom bar and a NavHost for the active mode.
 * Each mode gets its own NavHost, while shared application data is kept in
 * controllers above the individual mode navigation stacks.
 * Known issues: Firebase persistence and device-based user identification
 * are not connected yet.
 */

package com.example.code.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.code.controller.CampsiteController
import com.example.code.ui.admin.AdminCampsitesScreen
import com.example.code.ui.common.PlaceholderScreen
import com.example.code.ui.common.ProfileScreen
import com.example.code.ui.entrant.CampsiteDetailsScreen
import com.example.code.ui.entrant.ExploreScreen
import com.example.code.ui.owner.OwnerCampsitesScreen
import com.example.code.ui.owner.RegisterCampsiteScreen

private const val CAMPSITE_DETAILS_ROUTE =
    "entrant/campsite/{campsiteId}"

/**
 * Root composable of the app.
 *
 * @param controller keeps the active user mode.
 * @param campsiteController manages campsite data shared across modes.
 */
@Composable
fun AppNavHost(
    controller: ModeController = viewModel(),
    campsiteController: CampsiteController = viewModel(),
) {
    val mode by controller.activeMode.collectAsState()

    key(mode) {
        ModeShell(
            mode = mode,
            availableModes = controller.availableModes,
            onSwitchMode = { controller.switchTo(it) },
            campsiteController = campsiteController,
        )
    }
}

/**
 * Scaffold and NavHost for one mode.
 *
 * @param mode the active mode.
 * @param availableModes modes shown on the Profile mode switch.
 * @param onSwitchMode called when the user chooses another mode.
 * @param campsiteController shared controller for campsite data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeShell(
    mode: AppMode,
    availableModes: List<AppMode>,
    onSwitchMode: (AppMode) -> Unit,
    campsiteController: CampsiteController,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val campsites by campsiteController.campsites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mode.label) },
                actions = {
                    if (mode == AppMode.ADMIN) {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    Destination.ADMIN_PROFILE.route
                                ) {
                                    launchSingleTop = true
                                }
                            },
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "Profile",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            BottomBar(
                tabs = Destination.tabsFor(mode),
                currentRoute = currentRoute,
                onTabSelected = {
                    navController.openTab(it, mode)
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.startFor(mode).route,
            modifier = Modifier.padding(padding),
        ) {
            Destination.entries
                .filter { it.mode == mode }
                .forEach { destination ->
                    composable(destination.route) {
                        DestinationContent(
                            destination = destination,
                            mode = mode,
                            availableModes = availableModes,
                            onSwitchMode = onSwitchMode,
                            navController = navController,
                            campsiteController = campsiteController,
                        )
                    }
                }

            if (mode == AppMode.ENTRANT) {
                composable(
                    route = CAMPSITE_DETAILS_ROUTE,
                    arguments = listOf(
                        navArgument("campsiteId") {
                            type = NavType.StringType
                        }
                    ),
                ) { detailsEntry ->
                    val campsiteId =
                        detailsEntry.arguments
                            ?.getString("campsiteId")

                    val campsite =
                        campsites.firstOrNull {
                            it.id == campsiteId
                        }

                    if (campsite == null) {
                        Text("Campsite not found")
                    } else {
                        CampsiteDetailsScreen(
                            campsite = campsite,
                            onBack = {
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shows the screen for [destination].
 */
@Composable
private fun DestinationContent(
    destination: Destination,
    mode: AppMode,
    availableModes: List<AppMode>,
    onSwitchMode: (AppMode) -> Unit,
    navController: NavHostController,
    campsiteController: CampsiteController,
) {
    val campsites by campsiteController.campsites.collectAsState()

    when (destination) {
        Destination.ENTRANT_EXPLORE -> ExploreScreen(
            campsites = campsites,
            onCampsiteClick = { campsite ->
                navController.navigate(
                    "entrant/campsite/${campsite.id}"
                )
            },
        )

        Destination.OWNER_CAMPSITES -> OwnerCampsitesScreen(
            campsites = campsites,
            onRegisterCampsite = {
                navController.navigate(
                    Destination.OWNER_REGISTER_CAMPSITE.route
                )
            },
        )

        Destination.OWNER_REGISTER_CAMPSITE ->
            RegisterCampsiteScreen(
                onRegister = { campsite ->
                    campsiteController.addCampsite(campsite)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                },
            )

        Destination.ADMIN_CAMPSITES ->
            AdminCampsitesScreen()

        Destination.ENTRANT_PROFILE,
        Destination.OWNER_PROFILE,
        Destination.ADMIN_PROFILE ->
            ProfileScreen(
                activeMode = mode,
                availableModes = availableModes,
                onSwitchMode = onSwitchMode,
            )

        else ->
            PlaceholderScreen(destination.label)
    }
}

/**
 * Opens [tab] of [mode] without stacking tabs on top of each other.
 */
private fun NavHostController.openTab(
    tab: Destination,
    mode: AppMode,
) {
    navigate(tab.route) {
        popUpTo(Destination.startFor(mode).route) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}