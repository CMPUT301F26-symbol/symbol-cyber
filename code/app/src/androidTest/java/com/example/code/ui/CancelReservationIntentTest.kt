/*
 * CancelReservationIntentTest.kt
 * UI (intent) tests for an entrant cancelling their reservation
 * (US 01.07.01). Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Reservation
import com.example.code.ui.entrant.EntrantReservationsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CancelReservationIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val reservation = Reservation(
        id = "lottery-jasper-nov_device-priya-01",
        campsiteName = "Jasper Riverside",
        entrantId = "device-priya-01",
        ownerId = "owner-device-7f3a",
        startDate = 1794636000000L,
        endDate = 1794808800000L,
    )

    @Test
    fun entrantConfirmsAndCancels() {
        var cancelled: Reservation? = null
        composeRule.setContent {
            EntrantReservationsScreen(reservations = listOf(reservation), onCancel = { cancelled = it })
        }

        composeRule.onNodeWithText("Cancel reservation").performClick()
        composeRule.onNodeWithText("Cancel this reservation?").assertExists()
        // The dialog's confirm button is the second "Cancel reservation".
        composeRule.onAllNodesWithText("Cancel reservation")[1].performClick()

        assertEquals(reservation, cancelled)
    }

    @Test
    fun entrantKeepsTheReservation() {
        var cancelled: Reservation? = null
        composeRule.setContent {
            EntrantReservationsScreen(reservations = listOf(reservation), onCancel = { cancelled = it })
        }

        composeRule.onNodeWithText("Cancel reservation").performClick()
        composeRule.onNodeWithText("Keep reservation").performClick()

        assertNull(cancelled)
    }

    @Test
    fun resultIsShown() {
        composeRule.setContent {
            EntrantReservationsScreen(
                reservations = emptyList(),
                onCancel = {},
                statusMessage = "Reservation for Jasper Riverside cancelled.",
            )
        }

        composeRule.onNodeWithText("Reservation for Jasper Riverside cancelled.").assertExists()
    }
}
