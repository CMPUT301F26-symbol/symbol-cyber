// A written review an entrant posted for a campsite, with a helper to find a campsite's reviews.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.model

/**
 * One review an entrant posted for a campsite.
 *
 * @property id the Firestore document id of the review.
 * @property entrantId the device id of the entrant who posted the review.
 * @property campsiteId the id of the campsite that was reviewed.
 * @property text what the entrant wrote.
 */
data class Review(
    val id: String = "",
    val entrantId: String = "",
    val campsiteId: String = "",
    val text: String = "",
)

/**
 * Returns only the reviews posted for one campsite.
 *
 * @param reviews every review in the app
 * @param campsiteId the id of the campsite
 * @return the reviews of that campsite, in the same order
 */
fun reviewsForCampsite(reviews: List<Review>, campsiteId: String): List<Review> {
    return reviews.filter { it.campsiteId == campsiteId }
}
