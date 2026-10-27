/*
 * BottomBar.kt
 * Bottom navigation bar. Shows the tabs of the active mode (mockups E02, O01, A01).
 * Known issues: the Scan icon is a stand-in until the QR spike (T7) picks a library.
 */
package com.example.code.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

/**
 * Bottom bar with one item per tab.
 *
 * @param tabs tabs of the active mode, from [Destination.tabsFor].
 * @param currentRoute route on screen now, used to highlight the selected tab.
 * @param onTabSelected called with the tapped tab.
 */
@Composable
fun BottomBar(
    tabs: List<Destination>,
    currentRoute: String?,
    onTabSelected: (Destination) -> Unit,
) {
    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tab.route == currentRoute,
                onClick = { onTabSelected(tab) },
                icon = { Icon(iconFor(tab), contentDescription = tab.label) },
                label = {
                    // Small text so 5 labels fit on narrow phones.
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                },
            )
        }
    }
}

/** Icon shown for [destination] on the bottom bar. */
private fun iconFor(destination: Destination): ImageVector = when (destination) {
    Destination.ENTRANT_EXPLORE -> Icons.Filled.Search
    Destination.ENTRANT_SCAN -> Icons.Filled.Add
    Destination.ENTRANT_RESERVATIONS, Destination.OWNER_RESERVATIONS -> Icons.Filled.DateRange
    Destination.ENTRANT_NOTIFICATIONS -> Icons.Filled.Notifications
    Destination.OWNER_CAMPSITES, Destination.ADMIN_CAMPSITES -> Icons.Filled.Home
    Destination.OWNER_LOTTERIES -> Icons.Filled.Star
    Destination.ADMIN_IMAGES -> Icons.Filled.Face
    Destination.ADMIN_LOGS -> Icons.AutoMirrored.Filled.List
    Destination.ENTRANT_PROFILE, Destination.OWNER_PROFILE,
    Destination.ADMIN_PROFILES, Destination.ADMIN_PROFILE,
        -> Icons.Filled.Person
}
