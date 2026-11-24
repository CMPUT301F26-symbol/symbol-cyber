// Unit tests for finding a campsite's ratings and working out its overall rating.
package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RatingTest {

    private val lakeLouiseFive = Rating(
        id = "rating-1",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-lake-louise",
        score = 5,
    )

    private val lakeLouiseTwo = Rating(
        id = "rating-2",
        entrantId = "entrant-device-8e1f",
        campsiteId = "site-lake-louise",
        score = 2,
    )

    private val watertonFour = Rating(
        id = "rating-3",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-waterton",
        score = 4,
    )

    @Test
    fun ratingsForCampsite_mixedCampsites_onlyThatCampsiteKept() {
        val result = ratingsForCampsite(listOf(lakeLouiseFive, watertonFour, lakeLouiseTwo), "site-lake-louise")

        // verify only that campsite's ratings are kept, in the same order
        assertEquals(listOf(lakeLouiseFive, lakeLouiseTwo), result)
    }

    @Test
    fun ratingsForCampsite_noRatings_emptyList() {
        // verify a campsite nobody rated gives an empty list
        assertEquals(emptyList<Rating>(), ratingsForCampsite(listOf(watertonFour), "site-lake-louise"))
    }

    @Test
    fun averageRating_twoRatings_averageReturned() {
        // verify 5 and 2 average to 3.5
        assertEquals(3.5, averageRating(listOf(lakeLouiseFive, lakeLouiseTwo))!!, 0.001)
    }

    @Test
    fun averageRating_oneRatingRemoved_averageLeavesItOut() {
        // the 2 star rating is removed, so only the 5 star rating is left
        val remaining = listOf(lakeLouiseFive, lakeLouiseTwo).filter { it.id != lakeLouiseTwo.id }

        // verify the removed rating is no longer counted
        assertEquals(5.0, averageRating(remaining)!!, 0.001)
    }

    @Test
    fun averageRating_noRatings_null() {
        // verify a campsite with no ratings has no overall rating
        assertNull(averageRating(emptyList()))
    }
}
