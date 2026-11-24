/* Verifies eligibility boundaries with completed, future and rejected reservations. */
package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CampsiteReviewTest {
    private val stay = Reservation(
        id = "lottery-alice",
        campsiteId = "pine-lake",
        entrantId = "alice",
        endDate = 800L,
    )

    @Test
    fun onlyCompletedStaysForThisEntrantAndCampsiteQualify() {
        val ineligible = listOf(
            stay.copy(entrantId = "bob"),
            stay.copy(campsiteId = "hidden-ridge"),
            stay.copy(endDate = 1000L),
            stay.copy(endDate = 0L),
            stay.copy(status = Reservation.STATUS_REJECTED),
        )
        assertNull(reviewableStay(ineligible, "pine-lake", "alice", emptySet(), 1000L))
        assertEquals(stay, reviewableStay(ineligible + stay, "pine-lake", "alice", emptySet(), 1000L))
    }

    @Test
    fun previouslyReviewedStaysAreExcluded() {
        val earlier = stay.copy(id = "earlier-alice", endDate = 600L)
        val reservations = listOf(earlier, stay)
        assertEquals(stay, reviewableStay(reservations, "pine-lake", "alice", emptySet(), 1000L))
        assertEquals(earlier, reviewableStay(reservations, "pine-lake", "alice", setOf(stay.id), 1000L))
        assertNull(reviewableStay(reservations, "pine-lake", "alice", setOf(stay.id, earlier.id), 1000L))
    }
}
