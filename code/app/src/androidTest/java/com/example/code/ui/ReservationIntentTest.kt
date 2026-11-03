/*
 * ReservationIntentTest.kt
 * UI (intent) tests for the result of the automatic draw (US 02.05.01): the
 * entrant sees on the campsite's lottery card whether they won, a winner
 * accepts the reservation, the entrant sees active reservations
 * (US 01.06.01) and the owner sees active reservations (US 02.07.01).
 * Screens get canned data, so no Firestore is needed.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import com.example.code.ui.common.formatDateRange
import com.example.code.ui.entrant.EntrantReservationsScreen
import com.example.code.ui.entrant.LotterySection
import com.example.code.ui.entrant.WinnerReservationCard
import com.example.code.ui.owner.OwnerReservationsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReservationIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val afterDraw = 1794232800000L // Nov 9, 2026
    private val bookingStart = 1794636000000L // Nov 14, 2026
    private val bookingEnd = 1794808800000L // Nov 16, 2026

    private val drawnLottery = Lottery(
        id = "lottery-jasper-nov",
        campsiteId = "camp-jasper-riverside",
        campsiteName = "Jasper Riverside",
        ownerId = "owner-device-7f3a",
        registrationStart = 1793545200000L,
        registrationEnd = 1794178800000L,
        bookingStart = bookingStart,
        bookingEnd = bookingEnd,
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

    private val reservation = Reservation(
        id = "lottery-jasper-nov_device-priya-01",
        lotteryId = "lottery-jasper-nov",
        campsiteId = "camp-jasper-riverside",
        campsiteName = "Jasper Riverside",
        entrantId = "device-priya-01",
        entrantName = "Priya Shah",
        ownerId = "owner-device-7f3a",
        startDate = bookingStart,
        endDate = bookingEnd,
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

    @Test
    fun winnerAcceptsReservationAfterDraw() {
        var accepted = false
        composeRule.setContent {
            LotterySection(
                lottery = drawnLottery,
                myEntry = priyaWon,
                now = afterDraw,
                busy = false,
                statusMessage = null,
                onJoin = {},
                onLeave = {},
                drawnContent = { entry ->
                    WinnerReservationCard(
                        myEntry = entry,
                        reservation = null,
                        busy = false,
                        statusMessage = null,
                        onAccept = { accepted = true },
                    )
                },
            )
        }

        composeRule.onNodeWithText("Accept reservation").performClick()
        assertTrue(accepted)
    }

    @Test
    fun acceptedReservationShowsDates() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon,
                reservation = reservation,
                busy = false,
                statusMessage = "Reservation confirmed.",
                onAccept = {},
            )
        }

        composeRule.onNodeWithText("Reserved: ${formatDateRange(bookingStart, bookingEnd)}").assertExists()
        composeRule.onNodeWithText("Accept reservation").assertDoesNotExist()
    }

    @Test
    fun entrantNotSelectedSeesNoAcceptButton() {
        composeRule.setContent {
            WinnerReservationCard(
                myEntry = priyaWon.copy(status = WaitingListEntry.STATUS_NOT_SELECTED),
                reservation = null,
                busy = false,
                statusMessage = null,
                onAccept = {},
            )
        }

        composeRule.onNodeWithText("Accept reservation").assertDoesNotExist()
    }

    @Test
    fun entrantSeesActiveReservations() {
        composeRule.setContent {
            EntrantReservationsScreen(reservations = listOf(reservation))
        }

        composeRule.onNodeWithText("Jasper Riverside").assertExists()
        composeRule.onNodeWithText("Stay: ${formatDateRange(bookingStart, bookingEnd)}").assertExists()
    }

    @Test
    fun entrantWithoutReservationsSeesMessage() {
        composeRule.setContent {
            EntrantReservationsScreen(reservations = emptyList())
        }

        composeRule.onNodeWithText("You have no active reservations.").assertExists()
    }

    @Test
    fun ownerSeesActiveReservations() {
        composeRule.setContent {
            OwnerReservationsScreen(reservations = listOf(reservation))
        }

        composeRule.onNodeWithText("Priya Shah").assertExists()
        composeRule.onNodeWithText("Jasper Riverside").assertExists()
        composeRule.onNodeWithText(formatDateRange(bookingStart, bookingEnd)).assertExists()
    }

    @Test
    fun ownerWithoutReservationsSeesMessage() {
        composeRule.setContent {
            OwnerReservationsScreen(reservations = emptyList())
        }

        composeRule.onNodeWithText("No active reservations.").assertExists()
    }
}
