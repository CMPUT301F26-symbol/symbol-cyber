/*
 * DrawController.kt
 * Runs the automatic lottery draw (US 02.05.01): once a lottery's
 * registration period has ended, picks winners from its waiting list,
 * records the result and sends each winner an in-app notification.
 * There is no server, so any device with the app open runs the draw; a
 * Firestore transaction makes sure only one of them succeeds.
 * Known issues: a lottery is drawn only when some device has the app open
 * after registration closes; entrants who were not selected get no
 * notification yet (US 01.03.02).
 */

package com.example.code.controller

import com.example.code.data.LotteryRepository
import com.example.code.data.NotificationRepository
import com.example.code.model.AppNotification
import com.example.code.model.DrawResult
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import kotlin.random.Random

/**
 * Draws lottery winners when registration has ended.
 *
 * @param lotteries where lotteries and waiting lists are read and updated.
 * @param notifications where winner notifications are saved.
 * @param clock current time in milliseconds; replaceable in tests.
 * @param random source of randomness; seeded in tests.
 */
class DrawController(
    private val lotteries: LotteryRepository,
    private val notifications: NotificationRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val random: Random = Random.Default,
) {

    /**
     * Draws [lottery] if it is due.
     *
     * @return the draw result, or null if the lottery was not due or another
     * device drew it first.
     */
    suspend fun drawIfDue(lottery: Lottery): DrawResult? {
        val now = clock()
        if (!isDue(lottery, now)) return null

        val entries = lotteries.getWaitingList(lottery.id)
        val result = pickWinners(entries, lottery.winnerCount, random)
        val performed = lotteries.completeDraw(
            lotteryId = lottery.id,
            winnerIds = result.winners.map { it.entrantId },
            notSelectedIds = result.notSelected.map { it.entrantId },
            drawnAt = now,
        )
        if (!performed) return null

        notifications.sendAll(
            result.winners.map { winner ->
                AppNotification(
                    recipientId = winner.entrantId,
                    lotteryId = lottery.id,
                    campsiteName = lottery.campsiteName,
                    type = AppNotification.TYPE_LOTTERY_WON,
                    title = "You won ${lottery.campsiteName}!",
                    message = "Open the campsite to accept your reservation.",
                    createdAt = now,
                )
            },
        )
        return result
    }

    /**
     * Draws every lottery in [all] that is due.
     *
     * @return number of lotteries drawn by this call.
     */
    suspend fun drawAllDue(all: List<Lottery>): Int =
        all.count { drawIfDue(it) != null }

    companion object {
        /**
         * Returns true if [lottery] has not been drawn and its registration
         * period has ended at [now].
         */
        fun isDue(lottery: Lottery, now: Long): Boolean =
            lottery.status == Lottery.STATUS_OPEN &&
                lottery.registrationEnd > 0L &&
                now >= lottery.registrationEnd

        /**
         * Picks [winnerCount] entrants from [entries] at random. If fewer
         * entrants joined than there are winners, everyone wins.
         *
         * @param random source of randomness; pass a seeded one in tests.
         * @throws IllegalArgumentException if [winnerCount] is less than 1.
         */
        fun pickWinners(
            entries: List<WaitingListEntry>,
            winnerCount: Int,
            random: Random = Random.Default,
        ): DrawResult {
            require(winnerCount >= 1) { "A lottery needs at least one winner" }
            val winners = entries.shuffled(random).take(winnerCount)
            val winnerIds = winners.map { it.entrantId }.toSet()
            return DrawResult(
                winners = winners,
                notSelected = entries.filter { it.entrantId !in winnerIds },
            )
        }
    }
}
