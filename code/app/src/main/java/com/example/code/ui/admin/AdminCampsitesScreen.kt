// Admin screen that lists every campsite, opens its details and lets the admin remove one. Known issues: lotteries and reservations of a removed campsite stay in Firestore.
// AI use: Claude (Anthropic) helped add tapping a campsite to open its details and resolve a merge conflict in its wiring.
package com.example.code.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite

/**
 * Admin home: browse every campsite, open one to see its details, and remove one when needed.
 *
 * The admin is asked to confirm before a campsite is removed.
 *
 * @param campsites every campsite in the app.
 * @param modifier optional modifier for the screen.
 * @param isLoading true while campsites are still loading from Firestore.
 * @param errorMessage the loading error, or null if loading worked.
 * @param onRetry called when the admin taps Retry after an error.
 * @param onCampsiteClick called with the campsite the admin tapped, to show its details.
 * @param onRemoveCampsite called with the campsite to remove and a way to show an error if removing fails.
 */
@Composable
fun AdminCampsitesScreen(
    campsites: List<Campsite>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onCampsiteClick: (Campsite) -> Unit = {},
    onRemoveCampsite: (Campsite, (String) -> Unit) -> Unit = { _, _ -> },
) {
    // The campsite the admin tapped Remove on, waiting to be confirmed
    var campsiteToRemove by remember { mutableStateOf<Campsite?>(null) }

    var removeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("All campsites", style = MaterialTheme.typography.headlineSmall)

        if (removeError != null) {
            Text(removeError!!)
        }

        when {
            // Still waiting for Firestore
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading campsites…")
            }

            // Firestore failed, so let the admin try again
            errorMessage != null -> {
                Text("Unable to load campsites. Please try again.")
                TextButton(onClick = onRetry) { Text("Retry") }
            }

            // Loaded, but there are no campsites at all
            campsites.isEmpty() -> Text(
                "No campsites to review yet.",
                style = MaterialTheme.typography.bodyMedium,
            )

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = campsites, key = { it.id }) { campsite ->
                    AdminCampsiteCard(
                        campsite = campsite,
                        onClick = { onCampsiteClick(campsite) },
                        onRemove = { campsiteToRemove = campsite },
                    )
                }
            }
        }
    }

    // Ask before removing, since it can't be undone
    val pending = campsiteToRemove
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { campsiteToRemove = null },
            title = { Text("Remove ${pending.name}?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        removeError = null
                        campsiteToRemove = null

                        // Show the error if removing fails
                        onRemoveCampsite(pending) { message ->
                            removeError = message
                        }
                    },
                    modifier = Modifier.testTag("confirmRemoveButton"), // for testing
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { campsiteToRemove = null },
                    modifier = Modifier.testTag("cancelRemoveButton"), // for testing
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Displays one campsite that can be tapped for details, with a button to remove it.
 *
 * @param campsite the campsite to show.
 * @param onClick called when the admin taps the card.
 * @param onRemove called when the admin taps Remove.
 */
@Composable
private fun AdminCampsiteCard(campsite: Campsite, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("campsiteCard-${campsite.id}"), // for testing
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(campsite.name, style = MaterialTheme.typography.titleMedium)

            Text(campsite.location)

            Text("Maximum occupancy: ${campsite.maxOccupancy}")

            Button(
                onClick = onRemove,
                modifier = Modifier.testTag("removeButton-${campsite.id}"), // for testing
            ) {
                Text("Remove")
            }
        }
    }
}
