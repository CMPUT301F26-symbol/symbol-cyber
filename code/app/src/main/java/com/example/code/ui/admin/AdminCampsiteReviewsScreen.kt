// Admin screen that lists the reviews of one campsite and lets the admin remove one that breaks the app policy. Known issue is that entrants can't post reviews in the app yet, so reviews are added in the Firebase console for now.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
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
import com.example.code.model.Review
import com.example.code.model.reviewsForCampsite

/**
 * Shows every review of one campsite, and lets the admin remove a review.
 *
 * The admin is asked to confirm before a review is removed.
 *
 * @param campsite the campsite whose reviews are shown, or null if it no longer exists.
 * @param reviews every review in the app, only the ones for this campsite are shown.
 * @param isLoading true while reviews are still loading from Firestore.
 * @param errorMessage the loading error, or null if loading worked.
 * @param onRetry called when the admin taps Retry after an error.
 * @param onBack called when the admin taps Back.
 * @param onRemoveReview called with the review to remove and a way to show an error if removing fails.
 */
@Composable
fun AdminCampsiteReviewsScreen(
    campsite: Campsite?,
    reviews: List<Review>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onBack: () -> Unit = {},
    onRemoveReview: (Review, (String) -> Unit) -> Unit = { _, _ -> },
) {
    // The review the admin tapped Remove on, waiting to be confirmed
    var reviewToRemove by remember { mutableStateOf<Review?>(null) }

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

        // The campsite was removed while its reviews were open
        if (campsite == null) {
            Text("Campsite not found. It may have been removed.")
            return@Column
        }

        val campsiteReviews = reviewsForCampsite(reviews, campsite.id)

        Text("Reviews for ${campsite.name}", style = MaterialTheme.typography.headlineSmall)

        if (removeError != null) {
            Text(removeError!!)
        }

        when {
            // Still waiting for Firestore
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading reviews…")
            }

            // Firestore failed, so let the admin try again
            errorMessage != null -> {
                Text("Unable to load reviews. Please try again.")
                TextButton(onClick = onRetry) { Text("Retry") }
            }

            // Loaded, but nobody has reviewed this campsite
            campsiteReviews.isEmpty() -> Text("No reviews for this campsite yet.")

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = campsiteReviews, key = { it.id }) { review ->
                    AdminReviewCard(
                        review = review,
                        onRemove = { reviewToRemove = review },
                    )
                }
            }
        }
    }

    // Ask before removing, since it can't be undone
    val pending = reviewToRemove
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { reviewToRemove = null },
            title = { Text("Remove this review?") },
            text = { Text("It will no longer be shown for this campsite. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        removeError = null
                        reviewToRemove = null

                        // Show the error if removing fails
                        onRemoveReview(pending) { message ->
                            removeError = message
                        }
                    },
                    modifier = Modifier.testTag("confirmRemoveReviewButton"), // for testing
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { reviewToRemove = null },
                    modifier = Modifier.testTag("cancelRemoveReviewButton"), // for testing
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Displays one review with who wrote it, and a button to remove it.
 *
 * @param review the review to show.
 * @param onRemove called when the admin taps Remove.
 */
@Composable
private fun AdminReviewCard(review: Review, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reviewCard-${review.id}"), // for testing
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(review.text)

            Text("Reviewed by ${review.entrantId}")

            Button(
                onClick = onRemove,
                modifier = Modifier.testTag("removeReviewButton-${review.id}"), // for testing
            ) {
                Text("Remove")
            }
        }
    }
}
