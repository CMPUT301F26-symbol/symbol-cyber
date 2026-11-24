// Unit tests for finding the reviews of a campsite.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewTest {

    private val lakeLouiseGood = Review(
        id = "review-1",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-lake-louise",
        text = "Great views of the lake and a quiet site.",
    )

    private val lakeLouiseRude = Review(
        id = "review-2",
        entrantId = "entrant-device-8e1f",
        campsiteId = "site-lake-louise",
        text = "Rude review that breaks the app policy.",
    )

    private val watertonGood = Review(
        id = "review-3",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-waterton",
        text = "Windy but beautiful.",
    )

    @Test
    fun reviewsForCampsite_mixedCampsites_onlyThatCampsiteKept() {
        val result = reviewsForCampsite(listOf(lakeLouiseGood, watertonGood, lakeLouiseRude), "site-lake-louise")

        // verify only that campsite's reviews are kept, in the same order
        assertEquals(listOf(lakeLouiseGood, lakeLouiseRude), result)
    }

    @Test
    fun reviewsForCampsite_reviewRemoved_noLongerIncluded() {
        // the rude review is removed
        val remaining = listOf(lakeLouiseGood, lakeLouiseRude).filter { it.id != lakeLouiseRude.id }

        // verify only the good review is left for the campsite
        assertEquals(listOf(lakeLouiseGood), reviewsForCampsite(remaining, "site-lake-louise"))
    }

    @Test
    fun reviewsForCampsite_noReviews_emptyList() {
        // verify a campsite nobody reviewed gives an empty list
        assertEquals(emptyList<Review>(), reviewsForCampsite(listOf(watertonGood), "site-lake-louise"))
    }
}
