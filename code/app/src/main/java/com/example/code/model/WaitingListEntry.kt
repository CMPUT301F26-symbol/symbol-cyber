/*
 * WaitingListEntry.kt
 * Model of one entrant on a lottery's waiting list
 * (Firestore: lotteries/{lotteryId}/waitingList/{entrantId}). Data only.
 * Known issues: the entrant's name comes from the device until the User
 * profile (US 01.09.01, US 01.07.03) is connected.
 */

package com.example.code.model

/**
 * One entrant on a waiting list.
 *
 * @property entrantId device ID of the entrant; also the Firestore document ID,
 * so an entrant can only be on a waiting list once.
 * @property entrantName name shown to the campsite owner.
 * @property joinedAt time the entrant joined, in milliseconds since the epoch.
 * @property status [STATUS_WAITING] before the draw, then [STATUS_WON] or
 * [STATUS_NOT_SELECTED].
 */
data class WaitingListEntry(
    val entrantId: String = "",
    val entrantName: String = "",
    val joinedAt: Long = 0L,
    val status: String = STATUS_WAITING,
) {

    companion object {
        /** On the waiting list; the lottery has not been drawn. */
        const val STATUS_WAITING = "WAITING"

        /** Drawn as a winner. */
        const val STATUS_WON = "WON"

        /** Not drawn. */
        const val STATUS_NOT_SELECTED = "NOT_SELECTED"
    }
}
