/*
 * ReservationRulesTest.kt
 * Unit tests for ReservationController's reservation rules: when a
 * reservation counts as active, and its document ID.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.TestData
import com.example.code.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationRulesTest {

    private val reservation = TestData.priyaReservation

    @Test
    fun reservationIsActiveBeforeAndDuringTheStay() {
        assertTrue(ReservationController.isActive(reservation, TestData.AFTER_REGISTRATION))
        assertTrue(ReservationController.isActive(reservation, TestData.BOOKING_END))
    }

    @Test
    fun reservationIsNotActiveAfterTheStay() {
        assertFalse(ReservationController.isActive(reservation, TestData.BOOKING_END + 1))
    }

    @Test
    fun rejectedReservationIsNotActive() {
        val rejected = reservation.copy(status = Reservation.STATUS_REJECTED)
        assertFalse(ReservationController.isActive(rejected, TestData.AFTER_REGISTRATION))
    }

    @Test
    fun idCombinesLotteryAndEntrant() {
        assertEquals(
            "lottery-jasper-nov_device-priya-01",
            ReservationController.reservationId("lottery-jasper-nov", "device-priya-01"),
        )
    }
}
