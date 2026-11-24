/*
 * OwnerPastReservationsIntentTest.kt
 * UI (intent) tests for the owner's past reservations (US 02.07.02).
 * Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Reservation
import com.example.code.ui.common.formatDateRange
import com.example.code.ui.owner.OwnerReservationsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerPastReservationsIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val pastStart = 1791957600000L // Oct 14, 2026
    private val pastEnd = 1792130400000L // Oct 16, 2026

    private val pastReservation = Reservation(
        id = "lottery-banff-oct_device-liam-02",
        lotteryId = "lottery-banff-oct",
        campsiteId = "camp-banff-tunnel",
        campsiteName = "Banff Tunnel Mountain",
        entrantId = "device-liam-02",
        entrantName = "Liam Chen",
        ownerId = "owner-device-7f3a",
        startDate = pastStart,
        endDate = pastEnd,
    )

    @Test
    fun ownerSeesPastReservations() {
        composeRule.setContent {
            OwnerReservationsScreen(
                bookedDates = emptyList(),
                reservations = emptyList(),
                pastReservations = listOf(pastReservation),
                statusMessage = null,
                onReject = { _, _ -> },
            )
        }

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Liam Chen"))
        composeRule.onNodeWithText("Past reservations").assertExists()
        composeRule.onNodeWithText("Liam Chen").assertExists()
        composeRule.onNodeWithText(formatDateRange(pastStart, pastEnd)).assertExists()
        composeRule.onNodeWithText("Status: Completed").assertExists()
    }

    @Test
    fun ownerWithoutPastReservationsSeesMessage() {
        composeRule.setContent {
            OwnerReservationsScreen(
                bookedDates = emptyList(),
                reservations = emptyList(),
                statusMessage = null,
                onReject = { _, _ -> },
            )
        }

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("No past reservations."))
        composeRule.onNodeWithText("No past reservations.").assertExists()
    }
}
