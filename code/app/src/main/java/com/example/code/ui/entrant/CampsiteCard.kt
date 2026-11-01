/*
 * CampsiteCard.kt
 * Displays summary information for a campsite in the entrant Explore list.
 * Known issues: campsite images are not shown in summary cards yet.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite

/**
 * Displays the summary information required for a campsite listing.
 *
 * @param campsite campsite to display.
 * @param onClick called when the entrant selects the campsite.
 */
@Composable
fun CampsiteCard(
    campsite: Campsite,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = campsite.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(campsite.location)

            Text(
                text = "Maximum occupancy: ${campsite.maxOccupancy}"
            )

            Text(
                text = campsite.rating?.let {
                    "Rating: $it"
                } ?: "No ratings yet"
            )
        }
    }
}