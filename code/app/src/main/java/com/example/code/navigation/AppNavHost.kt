/*
 * AppNavHost.kt
 * Navigation shell: top bar, bottom bar and a NavHost for the active mode.
 * Each mode gets its own NavHost, so switching mode (on Profile) starts
 * fresh on that mode's first tab, as in storyboard S2.
 * Known issues: feature screens are placeholders until their tasks are done.
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.code.ui.admin.AdminCampsitesScreen
import com.example.code.ui.common.PlaceholderScreen
import com.example.code.ui.common.ProfileScreen
import com.example.code.ui.entrant.ExploreScreen
import com.example.code.ui.owner.OwnerCampsitesScreen

/**
 * Root composable of the app.
 *
 * @param controller keeps the active mode; one per activity.
 */
@Composable
fun AppNavHost(controller: ModeController = viewModel()) {
    val mode by controller.activeMode.collectAsState()
    // A new mode gets a new navigation stack.
    key(mode) {
        ModeShell(
            mode = mode,
            availableModes = controller.availableModes,
            onSwitchMode = { controller.switchTo(it) },
        )
    }
}

/**
 * Scaffold and NavHost for one mode.
 *
 * @param mode the active mode.
 * @param availableModes modes shown on the Profile mode switch.
 * @param onSwitchMode called when the user picks another mode on Profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeShell(
    mode: AppMode,
    availableModes: List<AppMode>,
    onSwitchMode: (AppMode) -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mode.label) },
                actions = {
                    // Admin has no Profile tab, so the mode switch is reached from here.
                    if (mode == AppMode.ADMIN) {
                        IconButton(onClick = {
                            navController.navigate(Destination.ADMIN_PROFILE.route) {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile")
                        }
                    }
                },
            )
        },
        bottomBar = {
            BottomBar(
                tabs = Destination.tabsFor(mode),
                currentRoute = currentRoute,
                onTabSelected = { navController.openTab(it, mode) },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.startFor(mode).route,
            modifier = Modifier.padding(padding),
        ) {
            Destination.entries.filter { it.mode == mode }.forEach { destination ->
                composable(destination.route) {
                    DestinationContent(destination, mode, availableModes, onSwitchMode)
                }
            }
        }
    }
}

/**
 * Shows the screen for [destination]. Replace a placeholder here when its
 * feature screen is ready.
 */
@Composable
private fun DestinationContent(
    destination: Destination,
    mode: AppMode,
    availableModes: List<AppMode>,
    onSwitchMode: (AppMode) -> Unit,
) {
    when (destination) {
        Destination.ENTRANT_EXPLORE -> ExploreScreen()
        Destination.OWNER_CAMPSITES -> OwnerCampsitesScreen()
        Destination.ADMIN_CAMPSITES -> AdminCampsitesScreen()

        Destination.ENTRANT_PROFILE, Destination.OWNER_PROFILE, Destination.ADMIN_PROFILE ->
            ProfileScreen(
                activeMode = mode,
                availableModes = availableModes,
                onSwitchMode = onSwitchMode,
            )

        else -> PlaceholderScreen(destination.label)
    }
}

/** Opens [tab] of [mode] without stacking tabs on top of each other. */
private fun NavHostController.openTab(tab: Destination, mode: AppMode) {
    navigate(tab.route) {
        popUpTo(Destination.startFor(mode).route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
