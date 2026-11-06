/*
 * AppMode.kt
 * The three modes the app can run in. A user picks the active mode on the
 * Profile screen (mockup E12, US 03.11.01).
 * Available modes are derived from the loaded User profile's roles.
 * provides the user's roles.
 */
package com.example.code.navigation

import com.example.code.UserRole

/**
 * A mode of the app. Each mode has its own bottom bar and start screen.
 *
 * @property label text shown on the mode switch.
 */
enum class AppMode(val label: String) {
    /** Browses campsites, joins waiting lists, reserves. */
    ENTRANT("Entrant"),

    /** Registers campsites and runs lotteries. */
    OWNER("Owner"),

    /** Browses and removes content, reads logs. */
    ADMIN("Admin"),
}

/**
 * Converts a set of user roles into the app modes available to that user.
 */
fun modesForRoles(
    roles: Set<UserRole>,
): Set<AppMode> {
    return buildSet {
        if (UserRole.ENTRANT in roles) {
            add(AppMode.ENTRANT)
        }

        if (UserRole.CAMPSITE_OWNER in roles) {
            add(AppMode.OWNER)
        }

        if (UserRole.ADMIN in roles) {
            add(AppMode.ADMIN)
        }
    }
}
