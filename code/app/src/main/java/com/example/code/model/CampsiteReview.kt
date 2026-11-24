/* Models a public campsite review and identifies completed stays with no review. */
package com.example.code.model

import com.example.code.controller.pastReservationsForEntrant

/** A review is stored under the reservation ID within its campsite's reviews. */
data class CampsiteReview(
    val reservationId: String,
    val authorName: String,
    val body: String,
    val createdAt: Long,
)

const val MAX_REVIEW_LENGTH = 1000

/** Finds the most recent completed, non-rejected stay that can still be reviewed. */
fun reviewableStay(
    reservations: List<Reservation>,
    campsiteId: String,
    entrantId: String,
    reviewedReservationIds: Set<String>,
    now: Long,
): Reservation? {
    if (entrantId.isBlank() || campsiteId.isBlank()) return null
    return pastReservationsForEntrant(reservations, entrantId, now).firstOrNull {
        it.campsiteId == campsiteId && it.id.isNotBlank() && it.endDate > 0 &&
            it.id !in reviewedReservationIds
    }
}
