/*
 * AppMode.kt
 * The three modes the app can run in. A user picks the active mode on the
 * Profile screen (mockup E12, US 03.11.01).
 * Known issues: available modes are hard-coded until the User model (T3)
 * provides the user's roles.
 */
package com.example.code.navigation

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
