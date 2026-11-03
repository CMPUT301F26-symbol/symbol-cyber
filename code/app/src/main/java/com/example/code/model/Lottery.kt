/*
 * Lottery.kt
 * Model of a campsite lottery: the data saved for it in Firestore
 * (lotteries/{id}). It holds data only; the rules (is registration open,
 * is there room) are in LotteryController.
 * Known issues: the owner's "Create lottery" form (US 02.04.01, 02.04.03-05,
 * 02.04.07) is a separate task; until it is merged, lotteries are added in
 * the Firestore console.
 */

package com.example.code.model

/**
 * A lottery for one campsite. Times are milliseconds since the epoch.
 *
 * @property id Firestore document ID.
 * @property campsiteId campsite this lottery is for.
 * @property campsiteName campsite name, stored so lists need no extra read.
 * @property ownerId device ID of the campsite owner who created the lottery.
 * @property registrationStart time the waiting list opens.
 * @property registrationEnd time the waiting list closes and winners are drawn.
 * @property bookingStart first day of the stay that winners can book.
 * @property bookingEnd last day of the stay that winners can book.
 * @property winnerCount number of entrants to draw.
 * @property waitlistCapacity maximum entrants on the waiting list, or null for no limit.
 * @property waitingListCount entrants currently on the waiting list.
 * @property status [STATUS_OPEN] until the draw, then [STATUS_DRAWN].
 */
data class Lottery(
    val id: String = "",
    val campsiteId: String = "",
    val campsiteName: String = "",
    val ownerId: String = "",
    val registrationStart: Long = 0L,
    val registrationEnd: Long = 0L,
    val bookingStart: Long = 0L,
    val bookingEnd: Long = 0L,
    val winnerCount: Int = 1,
    val waitlistCapacity: Int? = null,
    val waitingListCount: Int = 0,
    val status: String = STATUS_OPEN,
) {

    companion object {
        /** Registration has not ended or winners have not been drawn yet. */
        const val STATUS_OPEN = "OPEN"

        /** Winners have been drawn. */
        const val STATUS_DRAWN = "DRAWN"
    }
}
