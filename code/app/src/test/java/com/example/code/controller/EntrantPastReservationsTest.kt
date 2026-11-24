/*
 * EntrantPastReservationsTest.kt
 * Unit tests for an entrant's past reservations (US 01.06.02).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.TestData
import com.example.code.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntrantPastReservationsTest {

    private val afterStay = TestData.BOOKING_END + 86_400_000L
    private val older = TestData.priyaReservation.copy(
        id = "older",
        startDate = TestData.REGISTRATION_START,
        endDate = TestData.REGISTRATION_END,
    )

    @Test
    fun finishedStaysArePastMostRecentFirst() {
        val all = listOf(older, TestData.priyaReservation, TestData.liamReservation)

        val past = pastReservationsForEntrant(all, "device-priya-01", afterStay)

        assertEquals(listOf(TestData.priyaReservation, older), past)
    }

    @Test
    fun upcomingStayIsNotPast() {
        val past = pastReservationsForEntrant(
            listOf(TestData.priyaReservation),
            "device-priya-01",
            TestData.AFTER_REGISTRATION,
        )
        assertTrue(past.isEmpty())
    }

    @Test
    fun rejectedReservationIsNotPast() {
        val rejected = TestData.priyaReservation.copy(status = Reservation.STATUS_REJECTED)
        assertTrue(pastReservationsForEntrant(listOf(rejected), "device-priya-01", afterStay).isEmpty())
    }

    @Test
    fun pastAndActiveDoNotOverlap() {
        val all = listOf(older, TestData.priyaReservation)
        val now = TestData.AFTER_REGISTRATION

        val active = ReservationController.activeForEntrant(all, "device-priya-01", now)
        val past = pastReservationsForEntrant(all, "device-priya-01", now)

        assertEquals(listOf(TestData.priyaReservation), active)
        assertEquals(listOf(older), past)
    }
}
