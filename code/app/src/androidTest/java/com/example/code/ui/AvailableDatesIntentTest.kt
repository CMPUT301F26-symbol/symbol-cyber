/*
 * AvailableDatesIntentTest.kt
 * UI (intent) tests for a lottery winner picking available dates before
 * reserving (US 01.04.01). Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
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
class AvailableDatesIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val day = 86_400_000L
    private val day1 = 1794636000000L // Nov 14, 2026
    private val day2 = day1 + day
    private val day3 = day2 + day

    private val priyaWon = WaitingListEntry("device-priya-01", "Priya Shah", 1793545260000L, WaitingListEntry.STATUS_WON)

    @Test
    fun winnerPicksFirstAndLastDayThenAccepts() {
        var chosen: Pair<Long, Long>? = null
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                availableDays = listOf(day1, day2, day3),
                onAcceptStay = { _, first, last -> chosen = first to last },
            )
        }

        composeRule.onNodeWithText("Choose your dates").assertExists()
        composeRule.onNodeWithText("Accept reservation").assertIsNotEnabled()
        composeRule.onNodeWithTag("arrive-$day1").performClick()
        composeRule.onNodeWithTag("leave-$day2").performClick()
        composeRule.onNodeWithText("Accept reservation").assertIsEnabled().performClick()

        assertEquals(day1 to day2, chosen)
    }

    @Test
    fun bookedDayIsNotOffered() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                availableDays = listOf(day1, day3),
                onAcceptStay = { _, _, _ -> },
            )
        }

        composeRule.onNodeWithTag("arrive-$day2").assertDoesNotExist()
        composeRule.onNodeWithTag("arrive-$day1").performClick()
        // Day 2 is booked, so a stay starting on day 1 can only end on day 1.
        composeRule.onNodeWithTag("leave-$day1").assertExists()
        composeRule.onNodeWithTag("leave-$day3").assertDoesNotExist()
    }

    @Test
    fun fullyBookedPeriodIsExplained() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                availableDays = emptyList(),
                onAcceptStay = { _, _, _ -> },
            )
        }

        composeRule.onNodeWithText("All dates in this booking period are already booked.").assertExists()
        composeRule.onNodeWithText("Accept reservation").assertIsNotEnabled()
    }
}
