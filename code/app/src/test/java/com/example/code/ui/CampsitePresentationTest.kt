package com.example.code.ui

import com.example.code.ui.entrant.campsiteRatingText
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CampsitePresentationTest {
    @Test fun missingAndInvalidRatingsUsePlaceholder() {
        for (value in listOf(null, 0.0, -1.0, 6.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertEquals("No ratings yet", campsiteRatingText(value))
        }
    }

    @Test fun ratingHasReadablePrecisionAndScale() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.CANADA)
            assertEquals("Rating: 4.3 / 5", campsiteRatingText(4.33333))
            assertEquals("Rating: 5.0 / 5", campsiteRatingText(5.0))
        } finally { Locale.setDefault(original) }
    }
}
