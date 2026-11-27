/*
 * AvailableDatesTest.kt
 * Unit tests for a lottery winner seeing and picking the dates still
 * available in the booking period (US 01.04.01).
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
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailableDatesTest {

    private val repository = FakeReservationRepository()
    private val controller = ReservationController(repository) { TestData.AFTER_REGISTRATION }
    private val lottery = TestData.jasperLottery.copy(status = Lottery.STATUS_DRAWN)
    private val priyaWon = TestData.priya.copy(status = WaitingListEntry.STATUS_WON)
    private val day1 = startOfDay(TestData.BOOKING_START)
    private val day2 = nextDay(day1)
    private val day3 = nextDay(day2)

    /** Liam already booked the middle day of the same campsite. */
    private val liamMiddle = TestData.priyaReservation.copy(
        id = "lottery-jasper-nov_device-liam-02",
        entrantId = "device-liam-02",
        entrantName = "Liam Chen",
        startDate = day2,
        endDate = day2,
    )

    @Test
    fun bookingPeriodHasOneEntryPerDay() {
        assertEquals(listOf(day1, day2, day3), bookingDays(lottery))
    }

    @Test
    fun daysBookedBySomeoneElseAreNotAvailable() {
        val available = availableDays(lottery, listOf(liamMiddle), "device-priya-01", lottery.campsiteId)
        assertEquals(listOf(day1, day3), available)
    }

    @Test
    fun bookingOnAnotherCampsiteDoesNotBlockDays() {
        val elsewhere = liamMiddle.copy(campsiteId = "camp-other")
        assertEquals(3, availableDays(lottery, listOf(elsewhere), "device-priya-01", lottery.campsiteId).size)
    }

    @Test
    fun rejectedReservationsDoNotBlockDays() {
        val rejected = liamMiddle.copy(status = Reservation.STATUS_REJECTED)
        assertEquals(3, availableDays(lottery, listOf(rejected), "device-priya-01", lottery.campsiteId).size)
    }

    @Test
    fun lastDayOptionsStopAtABookedDay() {
        val available = listOf(day1, day3)
        assertEquals(listOf(day1), endDayOptions(available, day1))
        assertEquals(listOf(day1, day2, day3), endDayOptions(listOf(day1, day2, day3), day1))
    }

    @Test
    fun winnerReservesTheChosenDays() = runBlocking {
        val reservation = controller.acceptWin(lottery, priyaWon, startDate = day3, endDate = endOfDay(day3), others = listOf(liamMiddle))

        assertEquals(day3, reservation!!.startDate)
        assertEquals(endOfDay(day3), reservation.endDate)
    }

    @Test
    fun winnerCannotReserveABookedDay() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.acceptWin(lottery, priyaWon, startDate = day1, endDate = day3, others = listOf(liamMiddle)) }
        }
        assertEquals(DATES_NOT_AVAILABLE, error.message)
        assertTrue(repository.reservations.value.isEmpty())
    }

    @Test
    fun datesOutsideTheBookingPeriodAreNotAvailable() {
        assertFalse(isStayAvailable(lottery, emptyList(), "device-priya-01", lottery.campsiteId, day1 - DAY_MILLIS, day1))
        assertFalse(isStayAvailable(lottery, emptyList(), "device-priya-01", lottery.campsiteId, day3, day1))
        assertTrue(isStayAvailable(lottery, emptyList(), "device-priya-01", lottery.campsiteId, day1, day3))
    }

    @Test
    fun lastDayIsOfferedWhenBookingEndsEarlierInTheDay() {
        // Starts at 3 pm on day 1 and ends at 11 am on day 3: day 3 must still be offered.
        val afternoon = lottery.copy(bookingStart = day1 + 15 * 3_600_000L, bookingEnd = day3 + 11 * 3_600_000L)
        assertEquals(listOf(day1, day2, day3), bookingDays(afternoon))
    }

    @Test
    fun stayEndsAtTheEndOfItsLastDay() {
        assertEquals(nextDay(day2) - 1, endOfDay(day2))
        assertTrue(ReservationController.isActive(liamMiddle.copy(endDate = endOfDay(day2)), day2 + 12 * 3_600_000L))
    }
}
