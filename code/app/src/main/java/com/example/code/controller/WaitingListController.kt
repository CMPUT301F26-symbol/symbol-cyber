/*
 * WaitingListController.kt
 * Controller for lotteries and their waiting lists. Finds the lottery to
 * show on a campsite's details screen (US 01.01.06).
 * Known issues: joining the waiting list is added with US 01.02.01.
 */

package com.example.code.controller

import com.example.code.data.LotteryRepository
import com.example.code.model.Lottery
import kotlinx.coroutines.flow.Flow

/**
 * Handles lottery and waiting list actions.
 *
 * @param lotteries source of lotteries.
 */
class WaitingListController(
    private val lotteries: LotteryRepository,
) {

    /** All lotteries, updated live. */
    val allLotteries: Flow<List<Lottery>> = lotteries.observeLotteries()

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
