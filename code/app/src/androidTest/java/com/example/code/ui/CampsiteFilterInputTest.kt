/*
 * CampsiteFilterInputTest.kt
 * Exercises the real browse filter controls, validation, clearing and live booked-date changes.
 * Known issues: uses realistic local fixtures; live Firestore is checked separately at the demo.
 */
package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.code.controller.CampsiteFilterController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.ui.entrant.ExploreScreen
import org.junit.Rule
import org.junit.Test

class CampsiteFilterInputTest {
    @get:Rule val compose = createComposeRule()
    private val dateController = CampsiteFilterController()
    private val stay = dateController.parse("", "", "2027-05-01", "2027-05-31").stay!!
    private val sites = listOf(
        Campsite(id = "jasper-river", name = "Jasper Riverside", location = "Jasper", maxOccupancy = 6),
        Campsite(id = "banff-pines", name = "Banff Pines", location = "Banff", maxOccupancy = 8),
    )
    private val lotteries = sites.map { Lottery(campsiteId = it.id, bookingStart = stay.start, bookingEnd = stay.end) }

    @Test fun combinedInputFiltersAndClearRestoresTheList() {
        compose.setContent { MaterialTheme { ExploreScreen(sites, {}, lotteries = lotteries) } }
        applyFilters("Jasper", "4", "2027-05-14", "2027-05-16")
        compose.onNodeWithText("Jasper Riverside").assertIsDisplayed()
        compose.onNodeWithText("Banff Pines").assertDoesNotExist()
        compose.onNodeWithText("Clear filters").performClick()
        compose.onNodeWithText("2 campsites found").assertIsDisplayed()
        applyFilters("", "99", "", "")
        compose.onNodeWithText("No campsites match your current filters.").assertIsDisplayed()
    }

    @Test fun badCalendarDateStaysInDialog() {
        compose.setContent { MaterialTheme { ExploreScreen(sites, {}, lotteries = lotteries) } }
        applyFilters("", "", "2027-02-30", "2027-03-01")
        compose.onNodeWithText("Enter a valid calendar date.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("2 campsites found").assertIsDisplayed()
    }

    @Test fun newBookingAndRejectionUpdateAppliedDates() {
        val reservations = mutableStateOf(emptyList<Reservation>())
        compose.setContent {
            MaterialTheme { ExploreScreen(sites.take(1), {}, lotteries = lotteries, reservations = reservations.value) }
        }
        applyFilters("Jasper", "4", "2027-05-14", "2027-05-16")
        val booked = dateController.parse("", "", "2027-05-14", "2027-05-16").stay!!
        compose.runOnIdle { reservations.value = listOf(Reservation(campsiteId = sites[0].id, startDate = booked.start, endDate = booked.end)) }
        compose.onNodeWithText("No campsites match your current filters.").assertIsDisplayed()
        compose.runOnIdle { reservations.value = reservations.value.map { it.copy(status = Reservation.STATUS_REJECTED) } }
        compose.onNodeWithText("Jasper Riverside").assertIsDisplayed()
    }

    private fun applyFilters(location: String, guests: String, start: String, end: String) {
        compose.onNodeWithText("Filters").performClick()
        for ((label, value) in listOf("Location" to location, "Guests" to guests, "Start date (YYYY-MM-DD)" to start, "End date (YYYY-MM-DD)" to end)) {
            if (value.isNotEmpty()) compose.onNodeWithText(label).performScrollTo().performTextInput(value)
        }
        compose.onNodeWithText("Apply filters").performClick()
    }
}
