/*
 * CancelReservationTest.kt
 * Unit tests for an entrant cancelling their reservation (US 01.07.01).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeReservationRepository
import com.example.code.fakes.TestData
import com.example.code.model.Reservation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelReservationTest {

    private val repository = FakeReservationRepository(listOf(TestData.priyaReservation))
    private val controller = ReservationController(repository) { TestData.AFTER_REGISTRATION }

    @Test
    fun entrantCancelsTheirReservation() = runBlocking {
        controller.cancel(TestData.priyaReservation, "device-priya-01")

        val saved = repository.reservations.value.getValue(TestData.priyaReservation.id)
        assertEquals(Reservation.STATUS_CANCELLED, saved.status)
    }

    @Test
    fun cancelledReservationIsNoLongerActive() = runBlocking {
        controller.cancel(TestData.priyaReservation, "device-priya-01")
        val all = repository.reservations.value.values.toList()

        assertTrue(ReservationController.activeForEntrant(all, "device-priya-01", TestData.AFTER_REGISTRATION).isEmpty())
        assertTrue(ReservationController.activeForOwner(all, TestData.OWNER_ID, TestData.AFTER_REGISTRATION).isEmpty())
    }

    @Test
    fun anotherEntrantCannotCancel() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.cancel(TestData.priyaReservation, "device-liam-02") }
        }
        assertEquals(Reservation.STATUS_ACTIVE, repository.reservations.value.getValue(TestData.priyaReservation.id).status)
    }

    @Test
    fun finishedOrRejectedReservationCannotBeCancelled() {
        val rejected = TestData.priyaReservation.copy(status = Reservation.STATUS_REJECTED)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.cancel(rejected, "device-priya-01") }
        }
        val later = ReservationController(repository) { TestData.BOOKING_END + 1 }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { later.cancel(TestData.priyaReservation, "device-priya-01") }
        }
    }
}
