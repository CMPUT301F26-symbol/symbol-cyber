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

    //Following function for drawWinners is from GPT6-Sol "Please add in a drawing N winners function"-11-03-2026
    fun drawWinners(
        lottery: Lottery,
        waitingList: List<WaitingListEntry>,
        now: Long = System.currentTimeMillis()
    ): Lottery {
        if (lottery.status == Lottery.STATUS_DRAWN) {
            return lottery
        }

        check(lottery.status == Lottery.STATUS_OPEN) {
            "Lottery is not available for drawing."
        }

        check(now >= lottery.registrationEnd) {
            "Registration has not ended."
        }

        require(lottery.winnerCount > 0) {
            "Winner count must be greater than zero."
        }

        val selectedIds = waitingList
            .map { it.entrantId }
            .distinct()
            .shuffled()
            .take(lottery.winnerCount)

        return lottery.copy(
            winnerIds = selectedIds,
            status = Lottery.STATUS_DRAWN
        )
    }
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
