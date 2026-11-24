// Unit tests for turning Firestore rating documents into ratings.
package com.example.code.controller

import com.example.code.model.Rating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RatingDocumentTest {

    @Test
    fun ratingFromDocument_allFields_ratingRead() {
        val data = mapOf(
            "entrantId" to "entrant-device-2c9d",
            "campsiteId" to "site-lake-louise",
            "score" to 4L,
        )

        // verify every field is read, and Firestore's Long score becomes an Int
        assertEquals(
            Rating(id = "rating-1", entrantId = "entrant-device-2c9d", campsiteId = "site-lake-louise", score = 4),
            ratingFromDocument("rating-1", data),
        )
    }

    @Test
    fun ratingFromDocument_noCampsite_skipped() {
        val data = mapOf("entrantId" to "entrant-device-2c9d", "score" to 4L)

        // verify a rating that doesn't belong to a campsite is skipped
        assertNull(ratingFromDocument("rating-1", data))
    }

    @Test
    fun ratingFromDocument_noScore_zeroScore() {
        val data = mapOf("entrantId" to "entrant-device-2c9d", "campsiteId" to "site-lake-louise")

        // verify a missing score is read as 0 instead of crashing
        assertEquals(0, ratingFromDocument("rating-1", data)?.score)
    }
}
