/*
 * LotteryController.kt
 * Rules about a lottery and its waiting list: whether registration is open
 * and whether the waiting list has room. The Lottery model only holds
 * data; other controllers, repositories and screens ask this controller.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.Lottery

/** Rules for lotteries. */
object LotteryController {

    /**
     * Returns true if entrants can join or leave the waiting list of
     * [lottery] at [now]: it is not drawn and [now] is inside the
     * registration period.
     */
    fun isRegistrationOpen(lottery: Lottery, now: Long): Boolean =
        lottery.status == Lottery.STATUS_OPEN &&
                now in lottery.registrationStart until lottery.registrationEnd

    /** Returns true if the waiting list of [lottery] has room for one more entrant. */
    fun hasRoom(lottery: Lottery): Boolean =
        lottery.waitlistCapacity == null || lottery.waitingListCount < lottery.waitlistCapacity
}
