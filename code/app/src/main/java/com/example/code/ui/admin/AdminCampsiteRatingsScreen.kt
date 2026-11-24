// Admin screen that lists the ratings of one campsite and lets the admin remove one that breaks the app policy. Known issue is that entrants can't give ratings in the app yet, so ratings are added in the Firebase console for now.
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
import com.example.code.model.Rating
import com.example.code.model.ratingsForCampsite
import com.example.code.ui.entrant.campsiteRatingText

/**
 * Shows the overall rating and every rating of one campsite, and lets the admin remove a rating.
 *
 * The admin is asked to confirm before a rating is removed.
 *
 * @param campsite the campsite whose ratings are shown, or null if it no longer exists.
 * @param ratings every rating in the app, only the ones for this campsite are shown.
 * @param isLoading true while ratings are still loading from Firestore.
 * @param errorMessage the loading error, or null if loading worked.
 * @param onRetry called when the admin taps Retry after an error.
 * @param onBack called when the admin taps Back.
 * @param onRemoveRating called with the rating to remove and a way to show an error if removing fails.
 */
@Composable
fun AdminCampsiteRatingsScreen(
    campsite: Campsite?,
    ratings: List<Rating>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onBack: () -> Unit = {},
    onRemoveRating: (Rating, (String) -> Unit) -> Unit = { _, _ -> },
) {
    // The rating the admin tapped Remove on, waiting to be confirmed
    var ratingToRemove by remember { mutableStateOf<Rating?>(null) }

    var removeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.testTag("backButton"), // for testing
        ) {
            Text("Back")
        }

        // The campsite was removed while its ratings were open
        if (campsite == null) {
            Text("Campsite not found. It may have been removed.")
            return@Column
        }

        val campsiteRatings = ratingsForCampsite(ratings, campsite.id)

        Text("Ratings for ${campsite.name}", style = MaterialTheme.typography.headlineSmall)

        Text(
            campsiteRatingText(campsite.rating),
            modifier = Modifier.testTag("overallRating"), // for testing
        )

        if (removeError != null) {
            Text(removeError!!)
        }

        when {
            // Still waiting for Firestore
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading ratings…")
            }

            // Firestore failed, so let the admin try again
            errorMessage != null -> {
                Text("Unable to load ratings. Please try again.")
                TextButton(onClick = onRetry) { Text("Retry") }
            }

            // Loaded, but nobody has rated this campsite
            campsiteRatings.isEmpty() -> Text("No ratings for this campsite yet.")

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = campsiteRatings, key = { it.id }) { rating ->
                    AdminRatingCard(
                        rating = rating,
                        onRemove = { ratingToRemove = rating },
                    )
                }
            }
        }
    }

    // Ask before removing, since it can't be undone
    val pending = ratingToRemove
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { ratingToRemove = null },
            title = { Text("Remove this rating?") },
            text = { Text("The overall rating is updated without it. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        removeError = null
                        ratingToRemove = null

                        // Show the error if removing fails
                        onRemoveRating(pending) { message ->
                            removeError = message
                        }
                    },
                    modifier = Modifier.testTag("confirmRemoveRatingButton"), // for testing
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { ratingToRemove = null },
                    modifier = Modifier.testTag("cancelRemoveRatingButton"), // for testing
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Displays one rating with who gave it, and a button to remove it.
 *
 * @param rating the rating to show.
 * @param onRemove called when the admin taps Remove.
 */
@Composable
private fun AdminRatingCard(rating: Rating, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ratingCard-${rating.id}"), // for testing
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Score: ${rating.score} / 5", style = MaterialTheme.typography.titleMedium)

            Text("Rated by ${rating.entrantId}")

            Button(
                onClick = onRemove,
                modifier = Modifier.testTag("removeRatingButton-${rating.id}"), // for testing
            ) {
                Text("Remove")
            }
        }
    }
}
