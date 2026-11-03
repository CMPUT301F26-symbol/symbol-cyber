/*
 * ReservationIntentTest.kt
 * UI (intent) tests for the result of the automatic draw (US 02.05.01): the
 * entrant sees on the campsite's lottery card whether they won.
 * Screens get canned data, so no Firestore is needed.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.example.code.ui.entrant.LotterySection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReservationIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val afterDraw = 1794232800000L // Nov 9, 2026

    private val drawnLottery = Lottery(
        id = "lottery-jasper-nov",
        campsiteId = "camp-jasper-riverside",
        campsiteName = "Jasper Riverside",
        ownerId = "owner-device-7f3a",
        registrationStart = 1793545200000L,
        registrationEnd = 1794178800000L,
        bookingStart = 1794636000000L, // Nov 14, 2026
        bookingEnd = 1794808800000L, // Nov 16, 2026
        winnerCount = 2,
        waitlistCapacity = 4,
        waitingListCount = 4,
        status = Lottery.STATUS_DRAWN,
    )

    private val priyaWon = WaitingListEntry(
        "device-priya-01",
        "Priya Shah",
        1793545260000L,
        WaitingListEntry.STATUS_WON,
    )

    @Test
    fun winnerSeesTheyWon() {
        composeRule.setContent {
            LotterySection(
                lottery = drawnLottery,
                myEntry = priyaWon,
                now = afterDraw,
                busy = false,
                statusMessage = null,
                onJoin = {},
                onLeave = {},
            )
        }

        composeRule.onNodeWithText("You won this lottery!").assertExists()
        composeRule.onNodeWithText("Join Waiting List").assertDoesNotExist()
    }

    @Test
    fun entrantNotSelectedIsTold() {
        composeRule.setContent {
            LotterySection(
                lottery = drawnLottery,
                myEntry = priyaWon.copy(status = WaitingListEntry.STATUS_NOT_SELECTED),
                now = afterDraw,
                busy = false,
                statusMessage = null,
                onJoin = {},
                onLeave = {},
            )
        }

        composeRule.onNodeWithText("You were not selected this time.").assertExists()
    }

    @Test
    fun entrantWhoDidNotJoinSeesDrawHappened() {
        composeRule.setContent {
            LotterySection(
                lottery = drawnLottery,
                myEntry = null,
                now = afterDraw,
                busy = false,
                statusMessage = null,
                onJoin = {},
                onLeave = {},
            )
        }

        composeRule.onNodeWithText("Winners have been drawn.").assertExists()
    }
}
