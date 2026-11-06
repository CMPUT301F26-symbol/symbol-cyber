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
import com.example.code.data.NotificationRepository
import com.example.code.data.ReservationRepository
import com.example.code.model.AppNotification
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

    override suspend fun getWaitingList(lotteryId: String): List<WaitingListEntry> =
        waitingLists.value[lotteryId].orEmpty()

    override suspend fun completeDraw(
        lotteryId: String,
        winnerIds: List<String>,
        notSelectedIds: List<String>,
        drawnAt: Long,
    ): Boolean {
        val lottery = lotteries.value.getValue(lotteryId)
        if (lottery.status != Lottery.STATUS_OPEN) return false
        lotteries.value = lotteries.value + (lotteryId to lottery.copy(status = Lottery.STATUS_DRAWN))
        waitingLists.value = waitingLists.value + (
            lotteryId to waitingLists.value[lotteryId].orEmpty().map {
                when (it.entrantId) {
                    in winnerIds -> it.copy(status = WaitingListEntry.STATUS_WON)
                    in notSelectedIds -> it.copy(status = WaitingListEntry.STATUS_NOT_SELECTED)
                    else -> it
                }
            }
        )
        return true
    }
}

/** [NotificationRepository] that records what was sent. */
class FakeNotificationRepository : NotificationRepository {

    /** Every notification sent so far. */
    val sent = mutableListOf<AppNotification>()

    override suspend fun sendAll(notifications: List<AppNotification>) {
        sent += notifications
    }

    override fun notificationsFor(
        recipientId: String,
    ): Flow<List<AppNotification>> {
        return flowOf(
            sent.filter { it.recipientId == recipientId },
        )
    }
}

/** [ReservationRepository] that keeps reservations in memory. */
class FakeReservationRepository(reservations: List<Reservation> = emptyList()) : ReservationRepository {

    /** Current reservations by ID. */
    val reservations = MutableStateFlow(reservations.associateBy { it.id })

    override fun observeReservations(): Flow<List<Reservation>> =
        reservations.map { it.values.toList() }

    override suspend fun create(reservation: Reservation): Boolean {
        if (reservation.id in reservations.value) return false
        reservations.value = reservations.value + (reservation.id to reservation)
        return true
    }

    override suspend fun reject(reservationId: String, reason: String) {
        val reservation = reservations.value.getValue(reservationId)
        val rejected = reservation.copy(
            status = Reservation.STATUS_REJECTED,
            rejectionReason = reason,
        )
        reservations.value = reservations.value + (reservationId to rejected)
    }
}
