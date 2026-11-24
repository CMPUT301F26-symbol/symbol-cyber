// Unit tests for turning Firestore review documents into reviews.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.controller

import com.example.code.model.Review
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewDocumentTest {

    @Test
    fun reviewFromDocument_allFields_reviewRead() {
        val data = mapOf(
            "entrantId" to "entrant-device-2c9d",
            "campsiteId" to "site-lake-louise",
            "text" to "Great views of the lake and a quiet site.",
        )

        // verify every field is read
        assertEquals(
            Review(
                id = "review-1",
                entrantId = "entrant-device-2c9d",
                campsiteId = "site-lake-louise",
                text = "Great views of the lake and a quiet site.",
            ),
            reviewFromDocument("review-1", data),
        )
    }

    @Test
    fun reviewFromDocument_noCampsite_skipped() {
        val data = mapOf("entrantId" to "entrant-device-2c9d", "text" to "Great views.")

        // verify a review that doesn't belong to a campsite is skipped
        assertNull(reviewFromDocument("review-1", data))
    }

    @Test
    fun reviewFromDocument_noText_emptyText() {
        val data = mapOf("entrantId" to "entrant-device-2c9d", "campsiteId" to "site-lake-louise")

        // verify missing text is read as empty instead of crashing
        assertEquals("", reviewFromDocument("review-1", data)?.text)
    }
}
