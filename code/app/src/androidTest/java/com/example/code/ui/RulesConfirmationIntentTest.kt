/*
 * RulesConfirmationIntentTest.kt
 * UI (intent) tests for a lottery winner confirming the campsite rules and
 * policies before confirming the reservation (US 02.11.01).
 * Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.WaitingListEntry
import com.example.code.ui.entrant.WinnerReservationCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RulesConfirmationIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val priyaWon = WaitingListEntry(
        "device-priya-01",
        "Priya Shah",
        1793545260000L,
        WaitingListEntry.STATUS_WON,
    )

    @Test
    fun winnerReadsRulesAndMustConfirmBeforeAccepting() {
        var accepted: Boolean? = null
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = { accepted = it },
                rules = "Quiet hours 10 pm to 7 am.",
                cancellationPolicy = "Free cancellation until 7 days before.",
            )
        }

        composeRule.onNodeWithText("Quiet hours 10 pm to 7 am.").assertExists()
        composeRule.onNodeWithText("Free cancellation until 7 days before.").assertExists()
        composeRule.onNodeWithText("Accept reservation").assertIsNotEnabled()

        composeRule.onNodeWithTag("confirmRules").performClick()
        composeRule.onNodeWithText("Accept reservation").assertIsEnabled().performClick()

        assertEquals(true, accepted)
    }

    @Test
    fun campsiteWithoutRulesShowsNoCheckbox() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
            )
        }

        composeRule.onNodeWithTag("confirmRules").assertDoesNotExist()
        composeRule.onNodeWithText("Accept reservation").assertIsEnabled()
    }
}
