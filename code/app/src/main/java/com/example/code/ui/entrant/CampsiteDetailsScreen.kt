/*
 * CampsiteDetailsScreen.kt
 * Displays detailed information for a campsite selected by an entrant.
 * Known issues: remote promotional image loading is not yet implemented.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite

/**
 * Displays detailed campsite information.
 *
 * @param campsite campsite being viewed.
 * @param onBack called when the entrant returns to the campsite list.
 */
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Text(
            text = campsite.name,
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(
            text = "Location",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(campsite.location)

        Text(
            text = "Description / amenities",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(campsite.description)

        Text(
            text = "Maximum occupancy",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(campsite.maxOccupancy.toString())

        Text(
            text = "Rating",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            campsite.rating?.toString()
                ?: "No ratings yet"
        )

        Text(
            text = "Map location",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            "${campsite.latitude}, ${campsite.longitude}"
        )

        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
        )

        if (campsite.tags.isEmpty()) {
            Text("No tags")
        } else {
            Text(campsite.tags.joinToString(", "))
        }

        Text(
            text = "Promotional image",
            style = MaterialTheme.typography.titleMedium,
        )

        if (campsite.imageUrl.isNullOrBlank()) {
            Text("No promotional image available")
        } else {
            Text(campsite.imageUrl)
        }
    }
}