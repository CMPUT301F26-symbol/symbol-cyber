/*
 * ExploreScreen.kt
 * Entrant entry screen (mockup E02). Placeholder for the campsite list,
 * which T6 fills in from Firestore (US 01.01.03, 01.01.04, 01.01.05).
 * Known issues: no data yet.
 */
package com.example.code.ui.entrant

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.code.ui.common.PlaceholderScreen

/**
 * Entrant home: list of campsites to browse.
 */
@Composable
fun ExploreScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Explore campsites",
        note = "Campsite list (US 01.01.03 - 01.01.05)",
        modifier = modifier,
    )
}
