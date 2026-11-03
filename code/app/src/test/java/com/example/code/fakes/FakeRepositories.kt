/*
 * FakeRepositories.kt
 * In-memory versions of the repositories so controllers can be tested
 * without Firestore ("canned data").
 * Known issues: none.
 */

package com.example.code.fakes

import com.example.code.data.LotteryRepository
import com.example.code.model.Lottery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** [LotteryRepository] that keeps lotteries in memory. */
class FakeLotteryRepository(lotteries: List<Lottery> = emptyList()) : LotteryRepository {

    /** Current lotteries by ID. */
    val lotteries = MutableStateFlow(lotteries.associateBy { it.id })

    override fun observeLotteries(): Flow<List<Lottery>> = lotteries.map { it.values.toList() }
}
