/*
 * AppNavHost.kt
 * Navigation shell: top bar, bottom bar and a NavHost for the active mode.
 * Each mode gets its own NavHost, while shared application data is kept in
 * controllers above the individual mode navigation stacks.
 * Lottery screens are wired in through LotteryRoutes.kt, reservation screens
 * through ReservationRoutes.kt, and AutomaticDraw.kt draws winners when
 * registration ends.
 */

package com.example.code.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.code.User
import com.example.code.controller.CampsiteController
import com.example.code.controller.DrawController
import com.example.code.controller.LotteryController
import com.example.code.controller.RatingController
import com.example.code.controller.ReviewController
import com.example.code.controller.ReservationController
import com.example.code.controller.UserController
import com.example.code.controller.WaitingListController
import com.example.code.data.FirestoreLotteryRepository
import com.example.code.data.FirestoreNotificationRepository
import com.example.code.data.FirestoreReservationRepository
import com.example.code.data.FirestoreUserRepository
import com.example.code.data.LotteryCreationRepository
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.ownedCampsites
import com.example.code.model.updateFromForm
import com.example.code.ui.admin.AdminCampsiteRatingsScreen
import com.example.code.ui.admin.AdminCampsiteReviewsScreen
import com.example.code.ui.admin.AdminCampsitesScreen
import com.example.code.ui.admin.AdminImagesScreen
import com.example.code.ui.admin.AdminProfileRemovalRoute
import com.example.code.ui.common.PlaceholderScreen
import com.example.code.ui.common.ProfileScreen
import com.example.code.ui.entrant.CampsiteDetailsDestination
import com.example.code.ui.entrant.CampsiteScanRoute
import com.example.code.ui.entrant.ExploreScreen
import com.example.code.ui.owner.OwnerCampsiteDetailsDestination
import com.example.code.ui.owner.OwnerCampsitesScreen
import com.example.code.ui.owner.RegisterCampsiteScreen
import kotlinx.coroutines.launch

private const val OWNER_CAMPSITE_DETAILS_ROUTE = "owner/campsite/{campsiteId}"

private const val OWNER_EDIT_CAMPSITE_ROUTE = "owner/campsite/{campsiteId}/edit"

private const val CAMPSITE_DETAILS_ROUTE =
    "entrant/campsite/{campsiteId}"

private const val ADMIN_CAMPSITE_DETAILS_ROUTE = "admin/campsite/{campsiteId}"

private const val ADMIN_CAMPSITE_RATINGS_ROUTE = "admin/campsite/{campsiteId}/ratings"

private const val ADMIN_CAMPSITE_REVIEWS_ROUTE = "admin/campsite/{campsiteId}/reviews"

private const val OWNER_SHARED_LOTTERIES_ROUTE = "owner/shared-lotteries"

private const val OWNER_WAITING_LIST_ROUTE =
    "owner/lottery/{lotteryId}/waiting-list"

/**
 * Root composable of the app.
 *
 * Loads or creates the device-based user profile before showing the main app.
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
        WaitingListController(
            FirestoreLotteryRepository(),
            FirestoreNotificationRepository(),
        )
    },
    drawController: DrawController = remember {
        DrawController(
            FirestoreLotteryRepository(),
            FirestoreNotificationRepository(),
        )
    },
    reservationController: ReservationController = remember {
        ReservationController(FirestoreReservationRepository())
    },
    ownerId: String? = null,
    lotteryCreationRepository: LotteryCreationRepository =
        remember { FirestoreLotteryRepository() },
) {
    val userController =
        remember {
            UserController(FirestoreUserRepository())
        }

    val coroutineScope = rememberCoroutineScope()
    val rawDeviceId = ownerId ?: rememberDeviceId()

    LaunchedEffect(rawDeviceId) {
        userController.loadOrCreateUser(rawDeviceId)
    }

    val currentUser by userController.currentUser.collectAsState()
    val userIsLoading by userController.isLoading.collectAsState()
    val userError by userController.errorMessage.collectAsState()
    val availableModes by controller.availableModes.collectAsState()

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            controller.setAvailableModes(
                modesForRoles(user.roles),
            )
        }
    }

    if (currentUser == null) {
        Text(
            text = userError ?: "Loading user profile...",
        )
        return
    }

    val user = currentUser!!
    val deviceId = user.deviceId
    val mode by controller.activeMode.collectAsState()

    val lotterySnapshot by
    waitingListController.allLotteries.collectAsState(initial = null)

    val lotteries = lotterySnapshot.orEmpty()

    AutomaticDraw(
        lotteries = lotteries,
        controller = drawController,
    )

    key(mode) {
        ModeShell(
            mode = mode,
            availableModes = availableModes,
            onSwitchMode = { controller.switchTo(it) },
            user = user,
            userIsSaving = userIsLoading,
            userError = userError,
            onSaveProfile = { name, email, phoneNumber ->
                coroutineScope.launch {
                    userController.updateProfile(
                        name = name,
                        email = email,
                        phoneNumber = phoneNumber,
                    )
                }
            },
            onNotificationPreferenceChange = { enabled ->
                coroutineScope.launch {
                    userController.updateNotificationPreference(enabled)
                }
            },
            campsiteController = campsiteController,
            waitingListController = waitingListController,
            reservationController = reservationController,
            lotteries = lotteries,
            lotteriesReady = lotterySnapshot != null,
            deviceId = deviceId,
            lotteryCreationRepository = lotteryCreationRepository,
        )
    }
}

/**
 * Scaffold and NavHost for one mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeShell(
    mode: AppMode,
    availableModes: List<AppMode>,
    onSwitchMode: (AppMode) -> Unit,
    user: User,
    userIsSaving: Boolean,
    userError: String?,
    onSaveProfile: (String, String, String) -> Unit,
    onNotificationPreferenceChange: (Boolean) -> Unit,
    campsiteController: CampsiteController,
    waitingListController: WaitingListController,
    reservationController: ReservationController,
    lotteries: List<Lottery>,
    lotteriesReady: Boolean,
    deviceId: String,
    lotteryCreationRepository: LotteryCreationRepository,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val campsites by campsiteController.campsites.collectAsState()
    val isLoading by campsiteController.isLoading.collectAsState()
    val loadError by campsiteController.errorMessage.collectAsState()

    val reservationSnapshot by
    reservationController.allReservations.collectAsState(initial = null)

    val reservations = reservationSnapshot.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mode.label) },
                actions = {
                    if (mode == AppMode.ADMIN) {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    Destination.ADMIN_PROFILE.route,
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
                            user = user,
                            userIsSaving = userIsSaving,
                            userError = userError,
                            onSaveProfile = onSaveProfile,
                            onNotificationPreferenceChange = onNotificationPreferenceChange,
                            navController = navController,
                            campsiteController = campsiteController,
                            lotteries = lotteries,
                            reservations = reservations,
                            availabilityReady =
                                lotteriesReady &&
                                        reservationSnapshot != null,
                            reservationController = reservationController,
                            deviceId = deviceId,
                        )
                    }
                }

            if (mode == AppMode.OWNER) {
                composable(
                    route = OWNER_CAMPSITE_DETAILS_ROUTE,
                    arguments =
                        listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            },
                        ),
                ) { detailsEntry ->
                    OwnerCampsiteDetailsDestination(
                        campsiteId =
                            detailsEntry.arguments
                                ?.getString("campsiteId"),
                        ownerId = deviceId,
                        campsites = campsites,
                        isLoading = isLoading,
                        errorMessage = loadError,
                        onRetry = campsiteController::retryLoading,
                        onBack = { navController.popBackStack() },
                        //Following block(s) of code were generated by GPT-6 Sol, "Please look at my code base and implement User Story 2.04.02", November 17, 2026
                        // === US 02.04.02: BEGIN owner shared lottery publication ===
                        onPublishLottery = { selectedCampsites, settings ->
                            LotteryController.publishLottery(
                                repository = lotteryCreationRepository,
                                campsites = selectedCampsites,
                                ownerId = deviceId,
                                settings = settings,
                            )
                        },
                        // === US 02.04.02: END owner shared lottery publication ===
                        onEditCampsite = { campsite ->
                            navController.navigate(
                                "owner/campsite/${Uri.encode(campsite.id)}/edit",
                            )
                        },
                    )
                }

                composable(
                    route = OWNER_EDIT_CAMPSITE_ROUTE,
                    arguments =
                        listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            },
                        ),
                ) { editEntry ->
                    val campsiteId =
                        editEntry.arguments?.getString("campsiteId")

                    // Only look in the owner's own campsites, so nobody else can edit it
                    val campsite =
                        ownedCampsites(campsites, deviceId)
                            .firstOrNull { it.id == campsiteId }

                    if (campsite == null) {
                        Text("Campsite not found.")
                    } else {
                        RegisterCampsiteScreen(
                            campsite = campsite,
                            onRegister = { edited, showError ->
                                campsiteController.updateCampsite(
                                    updateFromForm(campsite, edited),
                                    // Only go back once it's saved
                                    onSuccess = {
                                        navController.popBackStack()
                                    },
                                    onFailure = { message ->
                                        showError(message)
                                    },
                                )
                            },
                            onBack = {
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }

            if (mode == AppMode.ENTRANT) {
                composable(
                    route = CAMPSITE_DETAILS_ROUTE,
                    arguments =
                        listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            },
                        ),
                ) { detailsEntry ->
                    val campsiteId =
                        detailsEntry.arguments
                            ?.getString("campsiteId")

                    val lottery =
                        WaitingListController.lotteryForCampsite(
                            lotteries,
                            campsiteId.orEmpty(),
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
                                            //Following block(s) of code were generated by GPT-6 Sol, "Please look at my code base and implement User Story 2.04.02", November 17, 2026
                                            // === US 02.04.02: BEGIN pass the currently viewed campsite for acceptance ===
                                            controller =
                                                reservationController,
                                            campsiteId = campsiteId,
                                            // === US 02.04.02: END pass the currently viewed campsite for acceptance ===
                                            campsite =
                                                campsites.firstOrNull { it.id == campsiteId },
                                        )
                                    }
                                },
                            )
                        },
                    )
                }
            }

            if (mode == AppMode.ADMIN) {
                composable(
                    route = ADMIN_CAMPSITE_DETAILS_ROUTE,
                    arguments =
                        listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            },
                        ),
                ) { detailsEntry ->
                    CampsiteDetailsDestination(
                        campsiteId =
                            detailsEntry.arguments
                                ?.getString("campsiteId"),
                        campsites = campsites,
                        isLoading = isLoading,
                        errorMessage = loadError,
                        onRetry = campsiteController::retryLoading,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(
                    route = ADMIN_CAMPSITE_RATINGS_ROUTE,
                    arguments =
                        listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            },
                        ),
                ) { ratingsEntry ->
                    // Only listens for ratings while this screen is open
                    val ratingController: RatingController = viewModel()
                    val ratings by ratingController.ratings.collectAsState()
                    val ratingsLoading by ratingController.isLoading.collectAsState()
                    val ratingsError by ratingController.errorMessage.collectAsState()

                    val campsiteId = ratingsEntry.arguments?.getString("campsiteId")

                    AdminCampsiteRatingsScreen(
                        campsite = campsites.firstOrNull { it.id == campsiteId },
                        ratings = ratings,
                        isLoading = ratingsLoading,
                        errorMessage = ratingsError,
                        onRetry = ratingController::retryLoading,
                        onBack = { navController.popBackStack() },
                        onRemoveRating = { rating, showError ->
                            ratingController.removeRating(
                                rating,
                                onSuccess = {},
                                onFailure = { message ->
                                    showError(message)
                                },
                            )
                        },
                    )
                }

                composable(
                    route = ADMIN_CAMPSITE_REVIEWS_ROUTE,
                    arguments =
                        listOf(
                            navArgument("campsiteId") {
                                type = NavType.StringType
                            },
                        ),
                ) { reviewsEntry ->
                    // Only listens for reviews while this screen is open
                    val reviewController: ReviewController = viewModel()
                    val reviews by reviewController.reviews.collectAsState()
                    val reviewsLoading by reviewController.isLoading.collectAsState()
                    val reviewsError by reviewController.errorMessage.collectAsState()

                    val campsiteId = reviewsEntry.arguments?.getString("campsiteId")

                    AdminCampsiteReviewsScreen(
                        campsite = campsites.firstOrNull { it.id == campsiteId },
                        reviews = reviews,
                        isLoading = reviewsLoading,
                        errorMessage = reviewsError,
                        onRetry = reviewController::retryLoading,
                        onBack = { navController.popBackStack() },
                        onRemoveReview = { review, showError ->
                            reviewController.removeReview(
                                review,
                                onSuccess = {},
                                onFailure = { message ->
                                    showError(message)
                                },
                            )
                        },
                    )
                }
            }

            if (mode == AppMode.OWNER) {
                composable(route = OWNER_SHARED_LOTTERIES_ROUTE) {
                    SharedLotteriesRoute(
                        lotteries = lotteries,
                        campsites = campsites,
                        deviceId = deviceId,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(
                    route = OWNER_WAITING_LIST_ROUTE,
                    arguments =
                        listOf(
                            navArgument("lotteryId") {
                                type = NavType.StringType
                            },
                        ),
                ) { entry ->
                    val lotteryId =
                        entry.arguments?.getString("lotteryId")

                    val lottery =
                        lotteries.firstOrNull {
                            it.id == lotteryId
                        }

                    if (lottery == null) {
                        Text("Lottery not found")
                    } else {
                        OwnerWaitingListRoute(
                            lottery = lottery,
                            deviceId = deviceId,
                            controller = waitingListController,
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
    user: User,
    userIsSaving: Boolean,
    userError: String?,
    onSaveProfile: (String, String, String) -> Unit,
    onNotificationPreferenceChange: (Boolean) -> Unit,
    navController: NavHostController,
    campsiteController: CampsiteController,
    lotteries: List<Lottery>,
    reservations: List<Reservation>,
    availabilityReady: Boolean,
    reservationController: ReservationController,
    deviceId: String,
) {
    val campsites by campsiteController.campsites.collectAsState()

    val isLoading by campsiteController.isLoading.collectAsState()
    val loadError by campsiteController.errorMessage.collectAsState()

    if (
        deviceId.isBlank() &&
        destination in
        listOf(
            Destination.OWNER_CAMPSITES,
            Destination.OWNER_REGISTER_CAMPSITE,
        )
    ) {
        Text(
            "Your device identity is unavailable. Please reopen the app.",
        )
        return
    }

    when (destination) {
        Destination.ENTRANT_EXPLORE ->
            ExploreScreen(
                campsites = campsites,
                lotteries = lotteries,
                reservations = reservations,
                availabilityReady = availabilityReady,
                isLoading = isLoading,
                errorMessage = loadError,
                onRetry = campsiteController::retryLoading,
                onCampsiteClick = { campsite ->
                    navController.navigate(
                        "entrant/campsite/${Uri.encode(campsite.id)}",
                    )
                },
            )

        Destination.ENTRANT_SCAN ->
            CampsiteScanRoute(
                onOpenCampsite = { campsiteId ->
                    navController.navigate(
                        "entrant/campsite/${Uri.encode(campsiteId)}",
                    ) {
                        launchSingleTop = true
                    }
                },
            )

        Destination.OWNER_CAMPSITES ->
            OwnerCampsitesScreen(
                campsites =
                    ownedCampsites(
                        campsites,
                        deviceId,
                    ),
                isLoading = isLoading,
                errorMessage = loadError,
                onRetry = campsiteController::retryLoading,
                onCampsiteClick = { campsite ->
                    navController.navigate(
                        "owner/campsite/${Uri.encode(campsite.id)}",
                    )
                },
                onRegisterCampsite = {
                    navController.navigate(
                        Destination.OWNER_REGISTER_CAMPSITE.route,
                    )
                },
            )

        Destination.OWNER_REGISTER_CAMPSITE ->
            RegisterCampsiteScreen(
                onRegister = { campsite, showError ->
                    campsiteController.addCampsite(
                        campsite.copy(
                            ownerId = deviceId,
                        ),
                        onSuccess = {
                            navController.popBackStack(
                                Destination.OWNER_REGISTER_CAMPSITE.route,
                                inclusive = true,
                            )
                        },
                        onFailure = { message ->
                            showError(message)
                        },
                    )
                },
                onBack = {
                    navController.popBackStack()
                },
            )

        Destination.OWNER_LOTTERIES ->
            Column {
                // Opens the lotteries other owners can share campsites in
                OutlinedButton(
                    onClick = { navController.navigate(OWNER_SHARED_LOTTERIES_ROUTE) },
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .testTag("sharedLotteriesButton"), // for testing
                ) {
                    Text("Shared lotteries")
                }

                OwnerLotteriesRoute(
                    lotteries = lotteries,
                    deviceId = deviceId,
                    // The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.04.06 As a campsite owner, I want to update my campsite lottery listing. Include all UI elements, logic, and test cases", November 24, 2026.
                    campsites = campsites,
                    onOpenWaitingList = { lottery ->
                        navController.navigate(
                            "owner/lottery/${lottery.id}/waiting-list",
                        )
                    },
                )
            }

        Destination.ENTRANT_NOTIFICATIONS ->
            EntrantNotificationsRoute(
                deviceId = deviceId,
                repository = remember {
                    FirestoreNotificationRepository()
                },
                onOpenReservations = {
                    navController.navigate(
                        Destination.ENTRANT_RESERVATIONS.route,
                    ) {
                        launchSingleTop = true
                    }
                },
            )
        Destination.ENTRANT_RESERVATIONS ->
            EntrantReservationsRoute(
                reservations = reservations,
                deviceId = deviceId,
            )

        Destination.OWNER_RESERVATIONS ->
            OwnerReservationsRoute(
                reservations = reservations,
                deviceId = deviceId,
                controller = reservationController,
            )

        Destination.ADMIN_PROFILES ->
            AdminProfileRemovalRoute(
                currentProfileId = deviceId,
            )

        Destination.ADMIN_CAMPSITES ->
            AdminCampsitesScreen(
                campsites = campsites,
                isLoading = isLoading,
                errorMessage = loadError,
                onRetry = campsiteController::retryLoading,
                onCampsiteClick = { campsite ->
                    navController.navigate(
                        "admin/campsite/${Uri.encode(campsite.id)}",
                    )
                },
                onRatingsClick = { campsite ->
                    navController.navigate(
                        "admin/campsite/${Uri.encode(campsite.id)}/ratings",
                    )
                },
                onReviewsClick = { campsite ->
                    navController.navigate(
                        "admin/campsite/${Uri.encode(campsite.id)}/reviews",
                    )
                },
                onRemoveCampsite = { campsite, showError ->
                    campsiteController.removeCampsite(
                        campsite,
                        onSuccess = {},
                        onFailure = { message ->
                            showError(message)
                        },
                    )
                },
            )

        Destination.ADMIN_IMAGES ->
            AdminImagesScreen(
                campsites = campsites,
                isLoading = isLoading,
                errorMessage = loadError,
                onRetry = campsiteController::retryLoading,
                onRemoveImage = { campsite, showError ->
                    campsiteController.removeCampsiteImage(
                        campsite,
                        onSuccess = {},
                        onFailure = { message ->
                            showError(message)
                        },
                    )
                },
            )

        Destination.ENTRANT_PROFILE,
        Destination.OWNER_PROFILE,
        Destination.ADMIN_PROFILE ->
            ProfileScreen(
                user = user,
                activeMode = mode,
                availableModes = availableModes,
                isSaving = userIsSaving,
                errorMessage = userError,
                onSaveProfile = onSaveProfile,
                onNotificationPreferenceChange = onNotificationPreferenceChange,
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
        popUpTo(
            Destination.startFor(mode).route,
        ) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}