/*
 * DeclineReservationTest.kt
 * Unit tests for a lottery winner declining the reservation (US 01.04.03).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeReservationRepository
import com.example.code.fakes.TestData
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DeclineReservationTest {

    private val repository = FakeReservationRepository()
    private val controller = ReservationController(repository) { TestData.AFTER_REGISTRATION }
    private val lottery = TestData.jasperLottery.copy(status = Lottery.STATUS_DRAWN)
    private val priyaWon = TestData.priya.copy(status = WaitingListEntry.STATUS_WON)

    @Test
    fun winnerDeclines() = runBlocking {
        assertTrue(controller.declineWin(lottery, priyaWon))

        val saved = repository.reservations.value.getValue("lottery-jasper-nov_device-priya-01")
        assertEquals(STATUS_DECLINED, saved.status)
    }

    @Test
    fun declinedWinCannotBeAcceptedLater() = runBlocking {
        controller.declineWin(lottery, priyaWon)

        assertNull(controller.acceptWin(lottery, priyaWon))
        assertFalse(controller.declineWin(lottery, priyaWon))
    }

    @Test
    fun acceptedWinCannotBeDeclined() = runBlocking {
        controller.acceptWin(lottery, priyaWon)

        assertFalse(controller.declineWin(lottery, priyaWon))
        assertEquals(Reservation.STATUS_ACTIVE, repository.reservations.value.values.single().status)
    }

    @Test
    fun onlyAWinnerCanDecline() {
        val notSelected = TestData.liam.copy(status = WaitingListEntry.STATUS_NOT_SELECTED)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.declineWin(lottery, notSelected) }
        }
    }

    @Test
    fun declinedReservationIsNotActive() = runBlocking {
        controller.declineWin(lottery, priyaWon)
        val all = repository.reservations.value.values.toList()

        assertTrue(ReservationController.activeForEntrant(all, "device-priya-01", TestData.AFTER_REGISTRATION).isEmpty())
        assertTrue(ReservationController.activeForOwner(all, TestData.OWNER_ID, TestData.AFTER_REGISTRATION).isEmpty())
    }
}
