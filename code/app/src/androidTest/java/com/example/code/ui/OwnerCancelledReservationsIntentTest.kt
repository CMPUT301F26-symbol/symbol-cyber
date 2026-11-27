/*
 * OwnerCancelledReservationsIntentTest.kt
 * UI (intent) tests for the owner's cancelled reservations (US 02.07.03).
 * Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Reservation
import com.example.code.ui.owner.OwnerReservationsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerCancelledReservationsIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val base = Reservation(
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

    private fun show(cancelled: List<Reservation>) {
        composeRule.setContent {
            OwnerReservationsScreen(
                bookedDates = emptyList(),
                reservations = emptyList(),
                cancelledReservations = cancelled,
                statusMessage = null,
                onReject = { _, _ -> },
            )
        }
    }

    @Test
    fun ownerSeesReservationCancelledByEntrant() {
        show(listOf(base.copy(status = Reservation.STATUS_CANCELLED)))

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Status: Cancelled by the entrant"))
        composeRule.onNodeWithText("Cancelled reservations").assertExists()
        composeRule.onNodeWithText("Priya Shah").assertExists()
        composeRule.onNodeWithText("Status: Cancelled by the entrant").assertExists()
    }

    @Test
    fun ownerSeesRejectedReservationWithReason() {
        show(listOf(base.copy(status = Reservation.STATUS_REJECTED, rejectionReason = "Site closed")))

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Reason: Site closed"))
        composeRule.onNodeWithText("Status: Rejected by you").assertExists()
        composeRule.onNodeWithText("Reason: Site closed").assertExists()
    }

    @Test
    fun ownerWithoutCancellationsSeesMessage() {
        show(emptyList())

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("No cancelled reservations."))
        composeRule.onNodeWithText("No cancelled reservations.").assertExists()
    }
}
