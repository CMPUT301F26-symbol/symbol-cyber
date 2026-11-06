// Owner screen that lists the campsites they have registered.

package com.example.code.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite

/**
 * Owner home screen showing registered campsites.
 *
 * @param campsites campsites currently registered by the owner.
 * @param onRegisterCampsite called when the owner taps "+ Register campsite".
 * @param onCampsiteClick opens the selected owner's stored details.
 * @param isLoading true while campsites are still loading from Firestore.
 * @param errorMessage the loading error, or null if loading worked.
 * @param onRetry called when the owner taps Retry after an error.
 */
@Composable
fun OwnerCampsitesScreen(
    campsites: List<Campsite>,
    onRegisterCampsite: () -> Unit,
    modifier: Modifier = Modifier,
    onCampsiteClick: (Campsite) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "My campsites",
            style = MaterialTheme.typography.headlineSmall,
        )

        Button(
            onClick = onRegisterCampsite,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("+ Register campsite")
        }

        when {
            // Still waiting for Firestore
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading your campsites…")
            }

            // Firestore failed, so let the owner try again
            errorMessage != null -> {
                Text("Unable to load your campsites. Please try again.")
                TextButton(onClick = onRetry) { Text("Retry") }
            }

            // Loaded, but this owner has none
            campsites.isEmpty() -> Text(
                "You have not registered any campsites yet.",
                style = MaterialTheme.typography.bodyMedium,
            )

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = campsites,
                    key = { campsite -> campsite.id.ifBlank { campsite.name } },
                ) { campsite ->
                    OwnerCampsiteCard(campsite, onClick = { onCampsiteClick(campsite) })
                }
            }
        }
    }
}

/**
 * Displays basic information about one campsite owned by the user.
 */
@Composable
private fun OwnerCampsiteCard(campsite: Campsite, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                campsite.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(campsite.location)

            Text("Maximum occupancy: ${campsite.maxOccupancy}")
        }
    }
}