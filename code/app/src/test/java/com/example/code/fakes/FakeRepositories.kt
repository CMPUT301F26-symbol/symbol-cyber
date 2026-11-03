/*
 * FakeRepositories.kt
 * In-memory versions of the repositories so controllers can be tested
 * without Firestore ("canned data").
 * Known issues: none.
 */

package com.example.code.fakes

import com.example.code.controller.LotteryController
import com.example.code.data.JoinResult
import com.example.code.data.LotteryRepository
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** [LotteryRepository] that keeps lotteries and waiting lists in memory. */
class FakeLotteryRepository(lotteries: List<Lottery> = emptyList()) : LotteryRepository {

    /** Current lotteries by ID. */
    val lotteries = MutableStateFlow(lotteries.associateBy { it.id })

    /** Current waiting lists by lottery ID. */
    val waitingLists = MutableStateFlow<Map<String, List<WaitingListEntry>>>(emptyMap())

    override fun observeLotteries(): Flow<List<Lottery>> = lotteries.map { it.values.toList() }

    override fun observeWaitingList(lotteryId: String): Flow<List<WaitingListEntry>> =
        waitingLists.map { it[lotteryId].orEmpty() }

    override suspend fun joinWaitingList(lotteryId: String, entry: WaitingListEntry): JoinResult {
        val lottery = lotteries.value.getValue(lotteryId)
        val list = waitingLists.value[lotteryId].orEmpty()
        return when {
            list.any { it.entrantId == entry.entrantId } -> JoinResult.ALREADY_JOINED
            !LotteryController.hasRoom(lottery) -> JoinResult.FULL
            else -> {
                waitingLists.value = waitingLists.value + (lotteryId to list + entry)
                lotteries.value = lotteries.value +
                        (lotteryId to lottery.copy(waitingListCount = lottery.waitingListCount + 1))
                JoinResult.JOINED
            }
        }
    }

    override suspend fun leaveWaitingList(lotteryId: String, entrantId: String): Boolean {
        val list = waitingLists.value[lotteryId].orEmpty()
        if (list.none { it.entrantId == entrantId }) return false
        waitingLists.value = waitingLists.value + (lotteryId to list.filter { it.entrantId != entrantId })
        val lottery = lotteries.value.getValue(lotteryId)
        lotteries.value = lotteries.value +
                (lotteryId to lottery.copy(waitingListCount = lottery.waitingListCount - 1))
        return true
    }
}
