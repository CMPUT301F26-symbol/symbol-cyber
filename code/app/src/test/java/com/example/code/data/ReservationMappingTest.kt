/*
 * ReservationMappingTest.kt
 * Unit tests for converting reservations to and from Firestore fields.
 * Known issues: none.
 */

package com.example.code.data

import com.example.code.fakes.TestData
import com.example.code.model.Reservation
import org.junit.Assert.assertEquals
import org.junit.Test

class ReservationMappingTest {

    @Test
    fun reservationRoundTrips() {
        val reservation = TestData.priyaReservation
        assertEquals(reservation, reservationFromMap(reservation.id, reservationToMap(reservation)))
    }

    @Test
    fun missingStatusMeansActive() {
        val reservation = reservationFromMap("r1", mapOf("campsiteName" to "Elk Island"))
        assertEquals(Reservation.STATUS_ACTIVE, reservation.status)
        assertEquals("Elk Island", reservation.campsiteName)
    }
}
