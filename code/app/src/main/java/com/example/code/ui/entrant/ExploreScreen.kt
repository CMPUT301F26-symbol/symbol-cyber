/*
 * ExploreScreen.kt
 * Displays campsites available for entrants to browse.
 * Known issues: filtering and Firestore loading are not connected yet.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite

/**
 * Displays the campsites currently available to an entrant.
 *
 * @param campsites campsites to display.
 * @param onCampsiteClick called when an entrant selects a campsite.
 * @param modifier modifier applied to the screen.
 */
@Composable
fun ExploreScreen(
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (campsites.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("No campsites match your current filters.")
        }

        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = campsites,
            key = { campsite -> campsite.id },
        ) { campsite ->
            CampsiteCard(
                campsite = campsite,
                onClick = {
                    onCampsiteClick(campsite)
                },
            )
        }
    }
}