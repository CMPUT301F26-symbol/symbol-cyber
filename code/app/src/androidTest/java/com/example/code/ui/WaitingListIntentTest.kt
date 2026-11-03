/*
 * WaitingListIntentTest.kt
 * UI (intent) tests for the lottery card on the entrant's campsite details
 * screen (US 01.01.06). Screens get canned data, so no Firestore is needed.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Lottery
import com.example.code.ui.entrant.LotterySection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaitingListIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val duringRegistration = 1793728800000L // Nov 3, 2026, 12:00

    private val lottery = Lottery(
        id = "lottery-jasper-nov",
        campsiteId = "camp-jasper-riverside",
        campsiteName = "Jasper Riverside",
        ownerId = "owner-device-7f3a",
        registrationStart = 1793545200000L, // Nov 1, 09:00
        registrationEnd = 1794178800000L, // Nov 8, 17:00
        bookingStart = 1794636000000L, // Nov 14
        bookingEnd = 1794808800000L, // Nov 16
        winnerCount = 2,
        waitlistCapacity = 4,
        waitingListCount = 2,
    )

    @Test
    fun detailsShowTheActiveLottery() {
        composeRule.setContent {
            LotterySection(lottery = lottery, now = duringRegistration)
        }

        composeRule.onNodeWithText("Winners drawn: 2").assertExists()
        composeRule.onNodeWithText("Waiting list: 2 of 4 spots taken").assertExists()
        composeRule.onNodeWithText("Registration is open.").assertExists()
    }

    @Test
    fun lotteryNotYetOpenSaysWhenItOpens() {
        composeRule.setContent {
            LotterySection(lottery = lottery, now = 1793469600000L) // Oct 31
        }

        composeRule.onNodeWithText("Registration is open.").assertDoesNotExist()
    }

    @Test
    fun campsiteWithoutLotteryShowsMessage() {
        composeRule.setContent {
            LotterySection(lottery = null, now = duringRegistration)
        }

        composeRule.onNodeWithText("This campsite has no lottery yet.").assertExists()
    }
}
