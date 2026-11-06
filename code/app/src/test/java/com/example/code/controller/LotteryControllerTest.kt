/*
 * LotteryControllerTest.kt
 * Unit tests for LotteryController: when registration is open, when the
 * waiting list has room, and the name shown for an entrant.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.TestData
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LotteryControllerTest {

    private val lottery = TestData.jasperLottery

    @Test
    fun registrationIsClosedBeforeItStarts() {
        assertFalse(LotteryController.isRegistrationOpen(lottery, TestData.BEFORE_REGISTRATION))
    }

    @Test
    fun registrationIsOpenFromStartUntilEnd() {
        assertTrue(LotteryController.isRegistrationOpen(lottery, TestData.REGISTRATION_START))
        assertTrue(LotteryController.isRegistrationOpen(lottery, TestData.DURING_REGISTRATION))
    }

    @Test
    fun registrationClosesAtTheEndTime() {
        assertFalse(LotteryController.isRegistrationOpen(lottery, TestData.REGISTRATION_END))
        assertFalse(LotteryController.isRegistrationOpen(lottery, TestData.AFTER_REGISTRATION))
    }

    @Test
    fun registrationIsClosedOnceDrawn() {
        val drawn = lottery.copy(status = Lottery.STATUS_DRAWN)
        assertFalse(LotteryController.isRegistrationOpen(drawn, TestData.DURING_REGISTRATION))
    }

    @Test
    fun cappedListHasRoomUntilFull() {
        assertTrue(LotteryController.hasRoom(lottery.copy(waitingListCount = 3)))
        assertFalse(LotteryController.hasRoom(lottery.copy(waitingListCount = 4)))
    }

    @Test
    fun uncappedListAlwaysHasRoom() {
        assertTrue(LotteryController.hasRoom(TestData.banffLottery.copy(waitingListCount = 500)))
    }

    @Test
    fun displayNameUsesTheEntrantsName() {
        assertEquals("Priya Shah", LotteryController.entrantDisplayName(TestData.priya))
    }

    @Test
    fun displayNameFallsBackToShortDeviceId() {
        val entry = WaitingListEntry(entrantId = "a1b2c3d4e5f6", entrantName = "  ")
        assertEquals("Entrant a1b2c3", LotteryController.entrantDisplayName(entry))
    }

    @Test
    fun newEntryIsWaiting() {
        assertEquals(WaitingListEntry.STATUS_WAITING, TestData.liam.status)
    }
}
