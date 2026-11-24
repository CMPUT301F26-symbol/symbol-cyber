/*
 * OwnerPastReservationsTest.kt
 * Unit tests for an owner's past reservations (US 02.07.02).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.TestData
import com.example.code.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerPastReservationsTest {

    private val afterAllStays = TestData.LATER_END + 86_400_000L

    @Test
    fun ownerSeesFinishedStaysMostRecentFirst() {
        val all = listOf(TestData.priyaReservation, TestData.liamReservation)

        val past = pastReservationsForOwner(all, TestData.OWNER_ID, afterAllStays)

        assertEquals(listOf(TestData.liamReservation, TestData.priyaReservation), past)
    }

    @Test
    fun otherOwnersReservationsAreNotShown() {
        val other = TestData.priyaReservation.copy(id = "x", ownerId = "another-owner")

        assertTrue(pastReservationsForOwner(listOf(other), TestData.OWNER_ID, afterAllStays).isEmpty())
    }

    @Test
    fun upcomingAndRejectedReservationsAreNotPast() {
        val rejected = TestData.priyaReservation.copy(id = "r", status = Reservation.STATUS_REJECTED)
        val all = listOf(TestData.priyaReservation, rejected)

        assertTrue(pastReservationsForOwner(all, TestData.OWNER_ID, TestData.AFTER_REGISTRATION).isEmpty())
        assertEquals(
            listOf(TestData.priyaReservation),
            pastReservationsForOwner(all, TestData.OWNER_ID, afterAllStays),
        )
    }
}
