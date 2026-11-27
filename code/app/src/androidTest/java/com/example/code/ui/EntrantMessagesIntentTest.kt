/*
 * EntrantMessagesIntentTest.kt
 * UI (intent) tests for an entrant messaging the campsite owner about a
 * reservation they have (US 01.14.01). Screens get canned data, so no
 * Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Reservation
import com.example.code.ui.common.ConversationScreen
import com.example.code.ui.entrant.EntrantReservationsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantMessagesIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val reservation = Reservation(
        id = "lottery-jasper-nov_device-priya-01",
        lotteryId = "lottery-jasper-nov",
        campsiteId = "camp-jasper-riverside",
        campsiteName = "Jasper Riverside",
        entrantId = "device-priya-01",
        entrantName = "Priya Shah",
        ownerId = "owner-device-7f3a",
        startDate = 1794636000000L,
        endDate = 1794808800000L,
    )

    @Test
    fun entrantOpensMessagesFromAnActiveReservation() {
        var opened: Reservation? = null
        composeRule.setContent {
            EntrantReservationsScreen(reservations = listOf(reservation), onMessageOwner = { opened = it })
        }

        composeRule.onNodeWithText("Message owner").performClick()
        assertEquals(reservation, opened)
    }

    @Test
    fun pastReservationHasNoMessageButton() {
        composeRule.setContent {
            EntrantReservationsScreen(
                reservations = emptyList(),
                pastReservations = listOf(reservation),
                onMessageOwner = {},
            )
        }

        composeRule.onNodeWithText("Message owner").assertDoesNotExist()
    }

    @Test
    fun entrantSendsAMessageToTheOwner() {
        var sent: String? = null
        composeRule.setContent {
            ConversationScreen(
                title = "Jasper Riverside: messages with the owner",
                messages = emptyList(),
                myId = "device-priya-01",
                canSend = true,
                busy = false,
                errorMessage = null,
                onSend = { sent = it },
                onBack = {},
            )
        }

        composeRule.onNodeWithTag("messageInput").performTextInput("Can we arrive at 9 pm?")
        composeRule.onNodeWithTag("sendMessage").performClick()
        assertEquals("Can we arrive at 9 pm?", sent)
    }
}
