/*
 * AppNavHost.kt
 * Navigation shell: top bar, bottom bar and a NavHost for the active mode.
 * Each mode gets its own NavHost, while shared application data is kept in
 * controllers above the individual mode navigation stacks.
 * Lottery screens are wired in through LotteryRoutes.kt, reservation screens
 * through ReservationRoutes.kt, and AutomaticDraw.kt draws winners when
 * registration ends.
 * Known issues: device-based user identification uses the raw device ID
 * until the User model is connected.
 */

package com.example.code.navigation

import android.net.Uri

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
import androidx.compose.runtime.remember
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
import com.example.code.controller.DrawController
import com.example.code.controller.LotteryController
import com.example.code.controller.ReservationController
import com.example.code.controller.WaitingListController
import com.example.code.data.FirestoreLotteryRepository
import com.example.code.data.FirestoreNotificationRepository
import com.example.code.data.FirestoreReservationRepository
import com.example.code.data.LotteryCreationRepository
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.ownedCampsites
import com.example.code.ui.admin.AdminCampsitesScreen
import com.example.code.ui.common.PlaceholderScreen
import com.example.code.ui.common.ProfileScreen
import com.example.code.ui.entrant.CampsiteDetailsDestination
import com.example.code.ui.entrant.ExploreScreen
import com.example.code.ui.owner.OwnerCampsitesScreen
import com.example.code.ui.owner.OwnerCampsiteDetailsDestination
import com.example.code.ui.owner.RegisterCampsiteScreen

private const val OWNER_CAMPSITE_DETAILS_ROUTE = "owner/campsite/{campsiteId}"

private const val CAMPSITE_DETAILS_ROUTE =
    "entrant/campsite/{campsiteId}"

private const val OWNER_WAITING_LIST_ROUTE =
    "owner/lottery/{lotteryId}/waiting-list"

/**
 * Root composable of the app.
 *
 * @param controller keeps the active user mode.
 * @param campsiteController manages campsite data shared across modes.
 * @param waitingListController manages lotteries and waiting lists.
 * @param drawController draws winners when registration ends.
 * @param ownerId optional active user ID, shared with owner and lottery flows.
 * @param reservationController manages reservations.
 * @param lotteryCreationRepository saves owner-created lotteries in the shared collection.
 */
@Composable
fun AppNavHost(
    controller: ModeController = viewModel(),
    campsiteController: CampsiteController = viewModel(),
    waitingListController: WaitingListController = remember {
        WaitingListController(FirestoreLotteryRepository(), FirestoreNotificationRepository())
    },
    drawController: DrawController = remember {
        DrawController(FirestoreLotteryRepository(), FirestoreNotificationRepository())
    },
    reservationController: ReservationController = remember {
        ReservationController(FirestoreReservationRepository())
    },
    ownerId: String? = null,
    lotteryCreationRepository: LotteryCreationRepository = remember { FirestoreLotteryRepository() },
) {
    val mode by controller.activeMode.collectAsState()
    val deviceId = ownerId ?: rememberDeviceId()
    val lotteries by waitingListController.allLotteries.collectAsState(initial = emptyList())

    AutomaticDraw(lotteries = lotteries, controller = drawController)

    key(mode) {
        ModeShell(
            mode = mode,
            availableModes = controller.availableModes,
            onSwitchMode = { controller.switchTo(it) },
            campsiteController = campsiteController,
            waitingListController = waitingListController,
            reservationController = reservationController,
            lotteries = lotteries,
            deviceId = deviceId,
            lotteryCreationRepository = lotteryCreationRepository,
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
 * @param waitingListController shared controller for lotteries and waiting lists.
 * @param reservationController shared controller for reservations.
 * @param lotteries all lotteries, loaded once for every mode.
 * @param deviceId this device's ID.
 * @param lotteryCreationRepository persistence for the owner's creation form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeShell(
    mode: AppMode,
    availableModes: List<AppMode>,
    onSwitchMode: (AppMode) -> Unit,
    campsiteController: CampsiteController,
    waitingListController: WaitingListController,
    reservationController: ReservationController,
    lotteries: List<Lottery>,
    deviceId: String,
    lotteryCreationRepository: LotteryCreationRepository,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val campsites by campsiteController.campsites.collectAsState()
    val isLoading by campsiteController.isLoading.collectAsState()
    val loadError by campsiteController.errorMessage.collectAsState()
    val reservations by reservationController.allReservations
        .collectAsState(initial = emptyList())

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
                            lotteries = lotteries,
                            reservations = reservations,
                            reservationController = reservationController,
                            deviceId = deviceId,
                        )
                    }
                }

            if (mode == AppMode.OWNER) {
                composable(
                    route = OWNER_CAMPSITE_DETAILS_ROUTE,
                    arguments = listOf(navArgument("campsiteId") { type = NavType.StringType }),
                ) { detailsEntry ->
                    OwnerCampsiteDetailsDestination(
                        campsiteId = detailsEntry.arguments?.getString("campsiteId"),
                        ownerId = deviceId,
                        campsites = campsites,
                        isLoading = isLoading,
                        errorMessage = loadError,
                        onRetry = campsiteController::retryLoading,
                        onBack = { navController.popBackStack() },
                        onPublishLottery = { campsite, settings ->
                            LotteryController.publishLottery(
                                lotteryCreationRepository, campsite, deviceId, settings,
                            )
                        },
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

                    val lottery = WaitingListController.lotteryForCampsite(
                        lotteries, campsiteId.orEmpty(),
                    )
                    CampsiteDetailsDestination(
                        campsiteId = campsiteId,
                        campsites = campsites,
                        isLoading = isLoading,
                        errorMessage = loadError,
                        onRetry = campsiteController::retryLoading,
                        onBack = { navController.popBackStack() },
                        lotteryContent = {
                            EntrantLotteryCard(
                                lottery = lottery,
                                deviceId = deviceId,
                                controller = waitingListController,
                                drawnContent = { myEntry ->
                                    if (lottery != null) {
                                        WinnerReservationContent(
                                            lottery = lottery,
                                            myEntry = myEntry,
                                            reservations = reservations,
                                            controller = reservationController,
                                        )
                                    }
                                },
                            )
                        },
                    )
                }
            }

            if (mode == AppMode.OWNER) {
                composable(
                    route = OWNER_WAITING_LIST_ROUTE,
                    arguments = listOf(
                        navArgument("lotteryId") { type = NavType.StringType }
                    ),
                ) { entry ->
                    val lotteryId = entry.arguments?.getString("lotteryId")
                    val lottery = lotteries.firstOrNull { it.id == lotteryId }

                    if (lottery == null) {
                        Text("Lottery not found")
                    } else {
                        OwnerWaitingListRoute(
                            lottery = lottery,
                            deviceId = deviceId,
                            controller = waitingListController,
                            onBack = { navController.popBackStack() },
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
    lotteries: List<Lottery>,
    reservations: List<Reservation>,
    reservationController: ReservationController,
    deviceId: String,
) {
    val campsites by campsiteController.campsites.collectAsState()

    val isLoading by campsiteController.isLoading.collectAsState()
    val loadError by campsiteController.errorMessage.collectAsState()

    if (deviceId.isBlank() && destination in listOf(
            Destination.OWNER_CAMPSITES, Destination.OWNER_REGISTER_CAMPSITE,
        )) {
        Text("Your device identity is unavailable. Please reopen the app.")
        return
    }

    when (destination) {
        Destination.ENTRANT_EXPLORE -> ExploreScreen(
            campsites = campsites,
            isLoading = isLoading,
            errorMessage = loadError,
            onRetry = campsiteController::retryLoading,
            onCampsiteClick = { campsite ->
                navController.navigate(
                    "entrant/campsite/${Uri.encode(campsite.id)}"
                )
            },
        )

        Destination.OWNER_CAMPSITES -> OwnerCampsitesScreen(
            campsites = ownedCampsites(campsites, deviceId),
            onCampsiteClick = { campsite ->
                navController.navigate("owner/campsite/${Uri.encode(campsite.id)}")
            },
            onRegisterCampsite = {
                navController.navigate(
                    Destination.OWNER_REGISTER_CAMPSITE.route
                )
            },
        )

        Destination.OWNER_REGISTER_CAMPSITE ->
            RegisterCampsiteScreen(
                onRegister = { campsite ->
                    campsiteController.addCampsite(campsite.copy(ownerId = deviceId))
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                },
            )

        Destination.OWNER_LOTTERIES -> OwnerLotteriesRoute(
            lotteries = lotteries,
            deviceId = deviceId,
            onOpenWaitingList = { lottery ->
                navController.navigate("owner/lottery/${lottery.id}/waiting-list")
            },
        )

        Destination.ENTRANT_RESERVATIONS -> EntrantReservationsRoute(
            reservations = reservations,
            deviceId = deviceId,
        )

        Destination.OWNER_RESERVATIONS -> OwnerReservationsRoute(
            reservations = reservations,
            deviceId = deviceId,
            controller = reservationController,
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