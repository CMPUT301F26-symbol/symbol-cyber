/*
 * OwnerMessagesIntentTest.kt
 * UI (intent) tests for a campsite owner messaging the entrant who
 * reserved (US 02.12.01). Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Message
import com.example.code.model.Reservation
import com.example.code.ui.common.ConversationScreen
import com.example.code.ui.owner.OwnerReservationsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerMessagesIntentTest {

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
    fun ownerOpensMessagesFromAnActiveReservation() {
        var opened: Reservation? = null
        composeRule.setContent {
            OwnerReservationsScreen(
                bookedDates = emptyList(),
                reservations = listOf(reservation),
                statusMessage = null,
                onReject = { _, _ -> },
                onMessage = { opened = it },
            )
        }

        composeRule.onNodeWithText("Message entrant").performClick()
        assertEquals(reservation, opened)
    }

    @Test
    fun ownerSendsAMessage() {
        var sent: String? = null
        composeRule.setContent {
            ConversationScreen(
                title = "Jasper Riverside: messages with Priya Shah",
                messages = emptyList(),
                myId = "owner-device-7f3a",
                canSend = true,
                busy = false,
                errorMessage = null,
                onSend = { sent = it },
                onBack = {},
            )
        }

        composeRule.onNodeWithText("No messages yet. Say hello!").assertExists()
        composeRule.onNodeWithTag("sendMessage").assertIsNotEnabled()
        composeRule.onNodeWithTag("messageInput").performTextInput("Gate code is 4521")
        composeRule.onNodeWithTag("sendMessage").performClick()
        assertEquals("Gate code is 4521", sent)
    }

    @Test
    fun ownerSeesTheirMessagesAndTheEntrantsReplies() {
        composeRule.setContent {
            ConversationScreen(
                title = "Jasper Riverside: messages with Priya Shah",
                messages = listOf(
                    Message(id = "1", senderId = "owner-device-7f3a", senderName = "Sam", fromOwner = true, text = "Welcome!", sentAt = 1L),
                    Message(id = "2", senderId = "device-priya-01", senderName = "Priya Shah", text = "Thanks!", sentAt = 2L),
                ),
                myId = "owner-device-7f3a",
                canSend = true,
                busy = false,
                errorMessage = null,
                onSend = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("You").assertExists()
        composeRule.onNodeWithText("Welcome!").assertExists()
        composeRule.onNodeWithText("Priya Shah").assertExists()
        composeRule.onNodeWithText("Thanks!").assertExists()
    }

    @Test
    fun inactiveReservationCannotGetNewMessages() {
        composeRule.setContent {
            ConversationScreen(
                title = "Jasper Riverside",
                messages = emptyList(),
                myId = "owner-device-7f3a",
                canSend = false,
                busy = false,
                errorMessage = null,
                onSend = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithTag("messageInput").assertDoesNotExist()
        composeRule.onNodeWithText("This reservation is no longer active, so no new messages can be sent.").assertExists()
    }
}
