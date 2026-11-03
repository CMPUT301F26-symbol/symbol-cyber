/*
 * ReservationControllerTest.kt
 * Unit tests for ReservationController: a winner accepts a reservation and
 * entrants and owners see active reservations (US 01.06.01, 02.07.01),
 * owners see booked dates (US 02.06.01) and reject (US 02.08.01).
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeReservationRepository
import com.example.code.fakes.TestData
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationControllerTest {

    private val repository = FakeReservationRepository()
    private val controller = ReservationController(repository) { TestData.AFTER_REGISTRATION }

    private val drawnLottery = TestData.jasperLottery.copy(status = Lottery.STATUS_DRAWN)
    private val priyaWon = TestData.priya.copy(status = WaitingListEntry.STATUS_WON)

    @Test
    fun winnerAcceptsAndGetsTheBookingPeriod() = runBlocking {
        val reservation = controller.acceptWin(drawnLottery, priyaWon)

        assertEquals(TestData.priyaReservation, reservation)
        assertEquals(listOf(TestData.priyaReservation), controller.allReservations.first())
    }

    @Test
    fun acceptingTwiceCreatesOneReservation() = runBlocking {
        controller.acceptWin(drawnLottery, priyaWon)

        assertNull(controller.acceptWin(drawnLottery, priyaWon))
        assertEquals(1, repository.reservations.value.size)
    }

    @Test
    fun entrantNotSelectedCannotAccept() {
        val notSelected = TestData.liam.copy(status = WaitingListEntry.STATUS_NOT_SELECTED)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.acceptWin(drawnLottery, notSelected) }
        }
    }

    @Test
    fun cannotAcceptBeforeTheDraw() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.acceptWin(TestData.jasperLottery, priyaWon) }
        }
    }

    @Test
    fun ownerRejectsWithReason() = runBlocking {
        repository.create(TestData.priyaReservation)

        controller.reject(TestData.priyaReservation, "  Site closed for bear activity  ")

        val saved = repository.reservations.value.getValue(TestData.priyaReservation.id)
        assertEquals(Reservation.STATUS_REJECTED, saved.status)
        assertEquals("Site closed for bear activity", saved.rejectionReason)
    }

    @Test
    fun rejectingNeedsAReason() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.reject(TestData.priyaReservation, " ") }
        }
    }

    @Test
    fun rejectedReservationCannotBeRejectedAgain() {
        val rejected = TestData.priyaReservation.copy(status = Reservation.STATUS_REJECTED)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.reject(rejected, "Duplicate") }
        }
    }

    @Test
    fun entrantSeesOnlyTheirActiveReservations() {
        val all = listOf(
            TestData.liamReservation,
            TestData.priyaReservation,
            TestData.priyaReservation.copy(id = "old", endDate = TestData.REGISTRATION_START),
            TestData.priyaReservation.copy(id = "rejected", status = Reservation.STATUS_REJECTED),
        )

        val active = ReservationController.activeForEntrant(
            all,
            "device-priya-01",
            TestData.AFTER_REGISTRATION,
        )

        assertEquals(listOf(TestData.priyaReservation), active)
    }

    @Test
    fun ownerSeesActiveReservationsSoonestFirst() {
        val others = TestData.priyaReservation.copy(id = "x", ownerId = "another-owner")
        val all = listOf(TestData.liamReservation, others, TestData.priyaReservation)

        val active = ReservationController.activeForOwner(
            all,
            TestData.OWNER_ID,
            TestData.AFTER_REGISTRATION,
        )

        assertEquals(listOf(TestData.priyaReservation, TestData.liamReservation), active)
    }

    @Test
    fun bookedDatesAreGroupedByCampsite() {
        val secondJasper = TestData.liamReservation.copy(
            id = "y",
            campsiteId = "camp-jasper-riverside",
            campsiteName = "Jasper Riverside",
        )
        val all = listOf(secondJasper, TestData.priyaReservation, TestData.liamReservation)

        val booked = ReservationController.bookedDates(
            all,
            TestData.OWNER_ID,
            TestData.AFTER_REGISTRATION,
        )

        assertEquals(listOf("Banff Tunnel Mountain", "Jasper Riverside"), booked.map { it.campsiteName })
        assertEquals(
            listOf(
                TestData.BOOKING_START to TestData.BOOKING_END,
                TestData.LATER_START to TestData.LATER_END,
            ),
            booked[1].stays,
        )
    }

    @Test
    fun noReservationsMeansNoBookedDates() {
        val booked = ReservationController.bookedDates(
            emptyList(),
            TestData.OWNER_ID,
            TestData.AFTER_REGISTRATION,
        )
        assertTrue(booked.isEmpty())
    }

    @Test
    fun findsTheWinnersReservationForALottery() {
        val all = listOf(TestData.priyaReservation, TestData.liamReservation)
        assertEquals(
            TestData.priyaReservation,
            ReservationController.forLottery(all, "lottery-jasper-nov", "device-priya-01"),
        )
        assertNull(ReservationController.forLottery(all, "lottery-jasper-nov", "device-liam-02"))
    }
}
