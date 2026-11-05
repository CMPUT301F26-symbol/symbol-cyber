package com.example.code.ui

import com.example.code.ui.entrant.campsitePriceText
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CampsitePriceTest {
    @Test fun missingAndInvalidPricesDoNotBecomeFree() {
        for (price in listOf(null, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertEquals("Price not provided", campsitePriceText(price, "CAD"))
        }
    }

    @Test fun showsStoredCurrencyOnlyAndKeepsExplicitZeroPrice() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.CANADA)
            assertEquals("CAD 35.00", campsitePriceText(35.0, "CAD"))
            assertEquals("35.00", campsitePriceText(35.0, null))
            assertEquals("0.00", campsitePriceText(0.0, ""))
        } finally { Locale.setDefault(original) }
    }
}
