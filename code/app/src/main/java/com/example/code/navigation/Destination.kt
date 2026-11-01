/*
 * Destination.kt
 * Every top-level screen (route) in the app, grouped by mode. Tabs match the
 * bottom bars on the UI mockups (E02, O01, A01).
 * Known issues: detail screens (campsite details, lottery, etc.) are added
 * by the feature tasks, not here.
 */
package com.example.code.navigation

/**
 * A top-level screen of the app.
 *
 * @property route unique navigation route, "mode/screen".
 * @property label text shown on the bottom bar.
 * @property mode the mode this screen belongs to.
 * @property inBottomBar false for screens opened another way (e.g. admin profile).
 */
enum class Destination(
    val route: String,
    val label: String,
    val mode: AppMode,
    val inBottomBar: Boolean = true,
) {
    ENTRANT_EXPLORE("entrant/explore", "Explore", AppMode.ENTRANT),
    ENTRANT_SCAN("entrant/scan", "Scan", AppMode.ENTRANT),
    ENTRANT_RESERVATIONS("entrant/reservations", "Reservations", AppMode.ENTRANT),
    ENTRANT_NOTIFICATIONS("entrant/notifications", "Notifications", AppMode.ENTRANT),
    ENTRANT_PROFILE("entrant/profile", "Profile", AppMode.ENTRANT),

    OWNER_CAMPSITES("owner/campsites", "Campsites", AppMode.OWNER),

    OWNER_REGISTER_CAMPSITE(
        "owner/register-campsite",
        "Register campsite",
        AppMode.OWNER,
        inBottomBar = false,
    ),

    OWNER_LOTTERIES("owner/lotteries", "Lotteries", AppMode.OWNER),
    OWNER_RESERVATIONS("owner/reservations", "Reservations", AppMode.OWNER),
    OWNER_PROFILE("owner/profile", "Profile", AppMode.OWNER),

    ADMIN_CAMPSITES("admin/campsites", "Campsites", AppMode.ADMIN),
    ADMIN_PROFILES("admin/profiles", "Profiles", AppMode.ADMIN),
    ADMIN_IMAGES("admin/images", "Images", AppMode.ADMIN),
    ADMIN_LOGS("admin/logs", "Logs", AppMode.ADMIN),
    ADMIN_PROFILE("admin/profile", "Profile", AppMode.ADMIN, inBottomBar = false),
    ;

    companion object {
        /**
         * Returns the bottom bar tabs of [mode], in display order.
         */
        fun tabsFor(mode: AppMode): List<Destination> =
            entries.filter { it.mode == mode && it.inBottomBar }

        /**
         * Returns the first screen shown when [mode] becomes active.
         */
        fun startFor(mode: AppMode): Destination = tabsFor(mode).first()

        /**
         * Returns the Profile screen of [mode] (where the mode switch is).
         */
        fun profileFor(mode: AppMode): Destination = when (mode) {
            AppMode.ENTRANT -> ENTRANT_PROFILE
            AppMode.OWNER -> OWNER_PROFILE
            AppMode.ADMIN -> ADMIN_PROFILE
        }

        /**
         * Finds the destination with [route], or null if there is none.
         */
        fun fromRoute(route: String?): Destination? =
            entries.firstOrNull { it.route == route }
    }
}