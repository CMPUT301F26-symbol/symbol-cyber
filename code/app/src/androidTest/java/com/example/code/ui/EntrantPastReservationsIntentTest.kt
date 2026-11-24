/*
 * EntrantPastReservationsIntentTest.kt
 * UI (intent) tests for the entrant's past reservations (US 01.06.02).
 * Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Reservation
import com.example.code.ui.common.formatDateRange
import com.example.code.ui.entrant.EntrantReservationsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntrantPastReservationsIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val pastStart = 1791957600000L // Oct 14, 2026
    private val pastEnd = 1792130400000L // Oct 16, 2026

    private val pastReservation = Reservation(
        id = "lottery-banff-oct_device-priya-01",
        lotteryId = "lottery-banff-oct",
        campsiteId = "camp-banff-tunnel",
        campsiteName = "Banff Tunnel Mountain",
        entrantId = "device-priya-01",
        entrantName = "Priya Shah",
        ownerId = "owner-device-7f3a",
        startDate = pastStart,
        endDate = pastEnd,
    )

    @Test
    fun entrantSeesPastReservations() {
        composeRule.setContent {
            EntrantReservationsScreen(reservations = emptyList(), pastReservations = listOf(pastReservation))
        }

        composeRule.onNodeWithText("Past reservations").assertExists()
        composeRule.onNodeWithText("Banff Tunnel Mountain").assertExists()
        composeRule.onNodeWithText("Stay: ${formatDateRange(pastStart, pastEnd)}").assertExists()
        composeRule.onNodeWithText("Status: Completed").assertExists()
        composeRule.onNodeWithText("You have no active reservations.").assertExists()
    }

    @Test
    fun entrantWithoutPastReservationsSeesMessage() {
        composeRule.setContent {
            EntrantReservationsScreen(reservations = emptyList())
        }

        composeRule.onNodeWithText("You have no past reservations.").assertExists()
    }
}
