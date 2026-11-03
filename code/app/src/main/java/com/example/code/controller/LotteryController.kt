/*
 * LotteryController.kt
 * Rules about a lottery and its waiting list: whether registration is open
 * and whether the waiting list has room, and the name shown for an entrant. The Lottery and WaitingListEntry models only hold model only holds
 * data; other controllers, repositories and screens ask this controller.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry

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

    /**
     * Returns the name shown for [entry], falling back to a short device ID
     * when the entrant has no name.
     */
    fun entrantDisplayName(entry: WaitingListEntry): String =
        entry.entrantName.ifBlank { "Entrant ${entry.entrantId.take(6)}" }
}
