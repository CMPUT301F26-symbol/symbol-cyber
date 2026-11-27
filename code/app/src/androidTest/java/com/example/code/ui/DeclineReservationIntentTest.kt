/*
 * DeclineReservationIntentTest.kt
 * UI (intent) tests for a lottery winner declining the reservation
 * (US 01.04.03). Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.controller.STATUS_DECLINED
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import com.example.code.ui.entrant.WinnerReservationCard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeclineReservationIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val priyaWon = WaitingListEntry("device-priya-01", "Priya Shah", 1793545260000L, WaitingListEntry.STATUS_WON)

    @Test
    fun winnerConfirmsBeforeDeclining() {
        var declined = false
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                onDecline = { declined = true },
            )
        }

        composeRule.onNodeWithText("Decline").performClick()
        composeRule.onNodeWithText("Decline this reservation?").assertExists()
        composeRule.onNodeWithText("Decline reservation").performClick()
        assertTrue(declined)
    }

    @Test
    fun winnerCanChangeTheirMind() {
        var declined = false
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                onDecline = { declined = true },
            )
        }

        composeRule.onNodeWithText("Decline").performClick()
        composeRule.onNodeWithText("Keep it").performClick()
        assertFalse(declined)
        composeRule.onNodeWithText("Accept reservation").assertExists()
    }

    @Test
    fun declinedReservationIsShown() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = Reservation(id = "r", status = STATUS_DECLINED),
                busy = false,
                statusMessage = null,
                onAccept = {},
                onDecline = {},
            )
        }

        composeRule.onNodeWithText("You declined this reservation.").assertExists()
        composeRule.onNodeWithText("Accept reservation").assertDoesNotExist()
    }
}
