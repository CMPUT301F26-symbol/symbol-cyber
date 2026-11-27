/*
 * ChooseCampsiteIntentTest.kt
 * UI (intent) tests for the winner of a multiple-campsite lottery choosing
 * which campsite to reserve (US 01.04.02). Screens get canned data, so no
 * Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.LotteryCampsite
import com.example.code.model.WaitingListEntry
import com.example.code.ui.entrant.WinnerReservationCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChooseCampsiteIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val day1 = 1794636000000L
    private val priyaWon = WaitingListEntry("device-priya-01", "Priya Shah", 1793545260000L, WaitingListEntry.STATUS_WON)
    private val choices = listOf(
        LotteryCampsite("camp-jasper-riverside", "Jasper Riverside"),
        LotteryCampsite("camp-banff-tunnel", "Banff Tunnel Mountain"),
    )

    @Test
    fun winnerMustChooseACampsiteBeforeDates() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                availableDays = listOf(day1),
                onAcceptStay = { _, _, _ -> },
                campsiteChoices = choices,
                chosenCampsiteId = null,
            )
        }

        composeRule.onNodeWithText("Choose your campsite").assertExists()
        composeRule.onNodeWithText("Choose your dates").assertDoesNotExist()
        composeRule.onNodeWithText("Accept reservation").assertIsNotEnabled()
    }

    @Test
    fun winnerPicksACampsiteThenItsDates() {
        var picked: String? = null
        var accepted: Long? = null
        composeRule.setContent {
            var chosen by remember { mutableStateOf<String?>(null) }
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                availableDays = listOf(day1),
                onAcceptStay = { _, first, _ -> accepted = first },
                campsiteChoices = choices,
                chosenCampsiteId = chosen,
                onChooseCampsite = {
                    chosen = it
                    picked = it
                },
            )
        }

        composeRule.onNodeWithTag("chooseCampsite-camp-banff-tunnel").performClick()
        composeRule.onNodeWithTag("chooseCampsite-camp-banff-tunnel").assertIsSelected()
        composeRule.onNodeWithText("Choose your dates").assertExists()
        composeRule.onNodeWithTag("arrive-$day1").performClick()
        composeRule.onNodeWithTag("leave-$day1").performClick()
        composeRule.onNodeWithText("Accept reservation").performClick()

        assertEquals("camp-banff-tunnel", picked)
        assertEquals(day1, accepted)
    }

    @Test
    fun singleCampsiteLotteryShowsNoChoice() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
                campsiteChoices = choices.take(1),
                chosenCampsiteId = "camp-jasper-riverside",
            )
        }

        composeRule.onNodeWithText("Choose your campsite").assertDoesNotExist()
    }
}
