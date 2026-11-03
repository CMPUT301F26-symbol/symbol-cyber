/*
 * ReservationController.kt
 * Controller for reservations: a lottery winner accepts a reservation,
 * entrants see their active reservations (US 01.06.01), and owners see
 * active reservations (US 02.07.01) and booked dates (US 02.06.01) for
 * their campsites.
 * Known issues: entrants and owners are identified by the device ID until
 * the User model (US 01.09.01) is connected.
 */

package com.example.code.controller

import com.example.code.data.ReservationRepository
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.flow.Flow

/**
 * A campsite and the date ranges that are booked on it.
 *
 * @property campsiteName campsite name.
 * @property stays booked stays as (first day, last day), earliest first.
 */
data class BookedDates(
    val campsiteName: String,
    val stays: List<Pair<Long, Long>>,
)

/**
 * Handles reservations for entrants and campsite owners.
 *
 * @param reservations where reservations are read and saved.
 * @param clock current time in milliseconds; replaceable in tests.
 */
class ReservationController(
    private val reservations: ReservationRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    /** All reservations, updated live. */
    val allReservations: Flow<List<Reservation>> = reservations.observeReservations()

    /**
     * Creates the reservation for a winner of [lottery] for the lottery's
     * booking period.
     *
     * @param entry the winner's waiting list entry.
     * @return the reservation, or null if the winner had already accepted.
     * @throws IllegalArgumentException if [entry] did not win [lottery].
     */
    suspend fun acceptWin(lottery: Lottery, entry: WaitingListEntry): Reservation? {
        require(lottery.status == Lottery.STATUS_DRAWN) { "Winners have not been drawn yet" }
        require(entry.status == WaitingListEntry.STATUS_WON) { "Only a winner can accept" }
        val reservation = Reservation(
            id = reservationId(lottery.id, entry.entrantId),
            lotteryId = lottery.id,
            campsiteId = lottery.campsiteId,
            campsiteName = lottery.campsiteName,
            entrantId = entry.entrantId,
            entrantName = entry.entrantName,
            ownerId = lottery.ownerId,
            startDate = lottery.bookingStart,
            endDate = lottery.bookingEnd,
            createdAt = clock(),
        )
        return if (reservations.create(reservation)) reservation else null
    }

    companion object {
        /**
         * Returns true if [reservation] stands and its stay has not ended at [now].
         */
        fun isActive(reservation: Reservation, now: Long): Boolean =
            reservation.status == Reservation.STATUS_ACTIVE && reservation.endDate >= now

        /** Builds the document ID for [entrantId]'s reservation in [lotteryId]. */
        fun reservationId(lotteryId: String, entrantId: String): String = "${lotteryId}_$entrantId"

        /** Entrant [entrantId]'s active reservations at [now], soonest first (US 01.06.01). */
        fun activeForEntrant(all: List<Reservation>, entrantId: String, now: Long): List<Reservation> =
            all.filter { it.entrantId == entrantId && isActive(it, now) }.sortedBy { it.startDate }

        /** Active reservations at [now] for campsites of owner [ownerId] (US 02.07.01). */
        fun activeForOwner(all: List<Reservation>, ownerId: String, now: Long): List<Reservation> =
            all.filter { it.ownerId == ownerId && isActive(it, now) }.sortedBy { it.startDate }

        /**
         * Booked stays on each of owner [ownerId]'s campsites at [now],
         * grouped by campsite (US 02.06.01).
         */
        fun bookedDates(all: List<Reservation>, ownerId: String, now: Long): List<BookedDates> =
            activeForOwner(all, ownerId, now)
                .groupBy { it.campsiteName }
                .map { (name, list) -> BookedDates(name, list.map { it.startDate to it.endDate }) }
                .sortedBy { it.campsiteName }

        /** Entrant [entrantId]'s reservation from [lotteryId], or null. */
        fun forLottery(all: List<Reservation>, lotteryId: String, entrantId: String): Reservation? =
            all.firstOrNull { it.id == reservationId(lotteryId, entrantId) }
    }
}
