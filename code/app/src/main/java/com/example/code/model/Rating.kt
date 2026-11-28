// A rating an entrant gave a campsite, with helpers to find a campsite's ratings and their average.
package com.example.code.model

/**
 * One rating an entrant gave a campsite.
 *
 * @property id the Firestore document id of the rating.
 * @property entrantId the device id of the entrant who gave the rating.
 * @property campsiteId the id of the campsite that was rated.
 * @property score the rating from 1 to 5.
 */
data class Rating(
    val id: String = "",
    val entrantId: String = "",
    val campsiteId: String = "",
    val score: Int = 0,
    // The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.13.01. Make sure to look through existing code and add all UI elements, logic, and test cases", November 27, 2026.
    /** Reservation identifier: one rating document per completed stay (US 01.13.01). */
    val reservationId: String = "",
)

/**
 * Returns only the ratings given to one campsite.
 *
 * @param ratings every rating in the app
 * @param campsiteId the id of the campsite
 * @return the ratings of that campsite, in the same order
 */
fun ratingsForCampsite(ratings: List<Rating>, campsiteId: String): List<Rating> {
    return ratings.filter { it.campsiteId == campsiteId }
}

/**
 * Works out the overall rating from a list of ratings.
 *
 * @param ratings the ratings of one campsite
 * @return the average score, or null if there are no ratings
 */
fun averageRating(ratings: List<Rating>): Double? {
    if (ratings.isEmpty()) {
        return null
    }
    return ratings.map { it.score }.average()
}

