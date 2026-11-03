/*
 * WaitingListController.kt
 * Controller for lottery waiting lists: finds the lottery shown on a
 * campsite's details screen (US 01.01.06) and lets entrants join its
 * waiting list (US 01.02.01).
 * Known issues: the entrant ID is the device ID until the User model
 * (US 01.09.01) is connected.
 */

package com.example.code.controller

import com.example.code.data.JoinResult
import com.example.code.data.LotteryRepository
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.flow.Flow

/**
 * Handles waiting list actions.
 *
 * @param lotteries source of lotteries and waiting lists.
 * @param clock current time in milliseconds; replaceable in tests.
 */
class WaitingListController(
    private val lotteries: LotteryRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    /** All lotteries, updated live. */
    val allLotteries: Flow<List<Lottery>> = lotteries.observeLotteries()

    /** The waiting list of [lotteryId], updated live. */
    fun waitingList(lotteryId: String): Flow<List<WaitingListEntry>> =
        lotteries.observeWaitingList(lotteryId)

    /**
     * Adds entrant [entrantId] to the waiting list of [lottery] (US 01.02.01).
     *
     * @return [JoinResult.CLOSED] if registration is not open now; otherwise
     * the repository's result.
     */
    suspend fun join(lottery: Lottery, entrantId: String, entrantName: String): JoinResult {
        require(entrantId.isNotBlank()) { "Entrant ID is required" }
        val now = clock()
        if (!LotteryController.isRegistrationOpen(lottery, now)) return JoinResult.CLOSED
        val entry = WaitingListEntry(
            entrantId = entrantId,
            entrantName = entrantName.trim(),
            joinedAt = now,
        )
        return lotteries.joinWaitingList(lottery.id, entry)
    }

    companion object {
        /**
         * Returns the lottery shown on a campsite's details page: the open
         * lottery for [campsiteId] if there is one, otherwise the one with the
         * latest registration end, or null if the campsite has no lottery.
         */
        fun lotteryForCampsite(all: List<Lottery>, campsiteId: String): Lottery? {
            val forCampsite = all.filter { it.campsiteId == campsiteId }
            return forCampsite.firstOrNull { it.status == Lottery.STATUS_OPEN }
                ?: forCampsite.maxByOrNull { it.registrationEnd }
        }
    }
}
