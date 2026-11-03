/*
 * WaitingListIntentTest.kt
 * UI (intent) tests for the waiting list stories: on the campsite details
 * screen an entrant sees the active lottery (US 01.01.06) and joins and
 * leaves its waiting list (US 01.02.01, 01.02.02); the owner sees each
 * lottery's waiting list (US 02.09.01). Screens get canned data, so no
 * Firestore is needed.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.example.code.ui.entrant.LotterySection
import com.example.code.ui.owner.OwnerLotteriesScreen
import com.example.code.ui.owner.OwnerWaitingListScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    private val priya = WaitingListEntry("device-priya-01", "Priya Shah", 1793545260000L)
    private val liam = WaitingListEntry("device-liam-02", "Liam Chen", 1793545320000L)

    @Test
    fun entrantJoinsWaitingListFromDetails() {
        var joined = false
        composeRule.setContent {
            LotterySection(
                lottery = lottery,
                myEntry = null,
                now = duringRegistration,
                busy = false,
                statusMessage = null,
                onJoin = { joined = true },
                onLeave = {},
            )
        }

        composeRule.onNodeWithText("Winners drawn: 2").assertExists()
        composeRule.onNodeWithText("Waiting list: 2 of 4 spots taken").assertExists()
        composeRule.onNodeWithText("Join Waiting List").performClick()
        assertTrue(joined)
    }

    @Test
    fun joinedEntrantCanLeave() {
        var left = false
        composeRule.setContent {
            LotterySection(
                lottery = lottery,
                myEntry = priya,
                now = duringRegistration,
                busy = false,
                statusMessage = "You joined the waiting list.",
                onJoin = {},
                onLeave = { left = true },
            )
        }

        composeRule.onNodeWithText("You are on the waiting list.").assertExists()
        composeRule.onNodeWithText("You joined the waiting list.").assertExists()
        composeRule.onNodeWithText("Leave Waiting List").performClick()
        assertTrue(left)
    }

    @Test
    fun fullWaitingListHasNoJoinButton() {
        composeRule.setContent {
            LotterySection(
                lottery = lottery.copy(waitingListCount = 4),
                myEntry = null,
                now = duringRegistration,
                busy = false,
                statusMessage = null,
                onJoin = {},
                onLeave = {},
            )
        }

        composeRule.onNodeWithText("The waiting list is full.").assertExists()
        composeRule.onNodeWithText("Join Waiting List").assertDoesNotExist()
    }

    @Test
    fun lotteryNotYetOpenHasNoJoinButton() {
        composeRule.setContent {
            LotterySection(
                lottery = lottery,
                myEntry = null,
                now = 1793469600000L, // Oct 31
                busy = false,
                statusMessage = null,
                onJoin = {},
                onLeave = {},
            )
        }

        composeRule.onNodeWithText("Join Waiting List").assertDoesNotExist()
    }

    @Test
    fun campsiteWithoutLotteryShowsMessage() {
        composeRule.setContent {
            LotterySection(
                lottery = null,
                myEntry = null,
                now = duringRegistration,
                busy = false,
                statusMessage = null,
                onJoin = {},
                onLeave = {},
            )
        }

        composeRule.onNodeWithText("This campsite has no lottery yet.").assertExists()
    }

    @Test
    fun ownerOpensWaitingListFromLotteries() {
        var opened: Lottery? = null
        composeRule.setContent {
            OwnerLotteriesScreen(lotteries = listOf(lottery), onOpenWaitingList = { opened = it })
        }

        composeRule.onNodeWithText("Jasper Riverside").assertExists()
        composeRule.onNodeWithText("Waiting list (2)").performClick()
        assertEquals(lottery, opened)
    }

    @Test
    fun ownerSeesWhoJoined() {
        composeRule.setContent {
            OwnerWaitingListScreen(lottery = lottery, entries = listOf(priya, liam), onBack = {})
        }

        composeRule.onNodeWithText("Priya Shah").assertExists()
        composeRule.onNodeWithText("Liam Chen").assertExists()
        composeRule.onNodeWithText("2 entrants joined").assertExists()
    }

    @Test
    fun emptyWaitingListShowsMessage() {
        composeRule.setContent {
            OwnerWaitingListScreen(lottery = lottery, entries = emptyList(), onBack = {})
        }

        composeRule.onNodeWithText("No entrants have joined yet.").assertExists()
    }
}
