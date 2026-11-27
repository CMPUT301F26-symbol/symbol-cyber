/*
 * OwnerCancelledReservationsTest.kt
 * Unit tests for an owner's cancelled reservations (US 02.07.03).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.TestData
import com.example.code.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerCancelledReservationsTest {

    private val cancelled = TestData.priyaReservation.copy(status = Reservation.STATUS_CANCELLED)
    private val rejected = TestData.liamReservation.copy(
        status = Reservation.STATUS_REJECTED,
        rejectionReason = "Site closed",
    )

    @Test
    fun cancelledAndRejectedAreListedNewestStayFirst() {
        val all = listOf(cancelled, rejected, TestData.priyaReservation.copy(id = "active"))

        val list = cancelledReservationsForOwner(all, TestData.OWNER_ID)

        assertEquals(listOf(rejected, cancelled), list)
    }

    @Test
    fun activeReservationsAreNotCancelled() {
        val all = listOf(TestData.priyaReservation, TestData.liamReservation)
        assertTrue(cancelledReservationsForOwner(all, TestData.OWNER_ID).isEmpty())
    }

    @Test
    fun otherOwnersCancellationsAreNotShown() {
        val other = cancelled.copy(ownerId = "another-owner")
        assertTrue(cancelledReservationsForOwner(listOf(other), TestData.OWNER_ID).isEmpty())
    }

    @Test
    fun cancelledReservationIsNeitherActiveNorPast() {
        val now = TestData.AFTER_REGISTRATION
        assertTrue(ReservationController.activeForOwner(listOf(cancelled), TestData.OWNER_ID, now).isEmpty())
        assertTrue(pastReservationsForOwner(listOf(cancelled), TestData.OWNER_ID, Long.MAX_VALUE).isEmpty())
    }
}
