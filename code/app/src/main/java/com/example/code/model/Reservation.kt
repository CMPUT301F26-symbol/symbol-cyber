/*
 * Reservation.kt
 * Model of a campsite reservation made by a lottery winner
 * (Firestore: reservations/{lotteryId}_{entrantId}). Data only; the rules
 * (is it active, its ID) are in ReservationController.
 * Known issues: the stay is the lottery's whole booking period; choosing
 * dates (US 01.04.01) comes after the halfway checkpoint.
 */

package com.example.code.model

/**
 * A reservation of one campsite by one entrant. Times are milliseconds
 * since the epoch.
 *
 * @property id Firestore document ID, "{lotteryId}_{entrantId}", so a winner
 * can only reserve once per lottery.
 * @property lotteryId lottery the entrant won.
 * @property campsiteId campsite reserved.
 * @property campsiteName campsite name, for display.
 * @property entrantId device ID of the entrant.
 * @property entrantName entrant name shown to the owner.
 * @property ownerId device ID of the campsite owner.
 * @property startDate first day of the stay.
 * @property endDate last day of the stay.
 * @property status [STATUS_ACTIVE], [STATUS_REJECTED] or [STATUS_CANCELLED].
 * @property rejectionReason owner's reason, if rejected.
 * @property createdAt time the reservation was made.
 * @property rulesAccepted true if the entrant confirmed reading the campsite
 * rules and policies before reserving (US 02.11.01).
 */
data class Reservation(
    val id: String = "",
    val lotteryId: String = "",
    val campsiteId: String = "",
    val campsiteName: String = "",
    val entrantId: String = "",
    val entrantName: String = "",
    val ownerId: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val status: String = STATUS_ACTIVE,
    val rejectionReason: String = "",
    val createdAt: Long = 0L,
    val rulesAccepted: Boolean = false,
) {

    companion object {
        /** Reservation stands. */
        const val STATUS_ACTIVE = "ACTIVE"

        /** Owner rejected the reservation (US 02.08.01). */
        const val STATUS_REJECTED = "REJECTED"

        /** Entrant cancelled the reservation (US 01.07.01). */
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
