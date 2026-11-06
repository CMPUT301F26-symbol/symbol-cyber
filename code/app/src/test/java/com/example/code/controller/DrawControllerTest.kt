/*
 * DrawControllerTest.kt
 * Unit tests for DrawController (US 02.05.01): drawing when registration
 * ends, marking winners, notifying them, and drawing only once.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeLotteryRepository
import com.example.code.fakes.FakeNotificationRepository
import com.example.code.fakes.TestData
import com.example.code.model.AppNotification
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class DrawControllerTest {

    private val lotteries = FakeLotteryRepository(listOf(TestData.jasperLottery))
    private val notifications = FakeNotificationRepository()
    private var now = TestData.AFTER_REGISTRATION
    private val controller = DrawController(lotteries, notifications, { now }, Random(2026))

    private val lotteryId = TestData.jasperLottery.id

    @Before
    fun fillWaitingList() {
        lotteries.waitingLists.value = mapOf(
            lotteryId to listOf(TestData.priya, TestData.liam, TestData.sofia, TestData.noah),
        )
    }

    private fun lottery(): Lottery = lotteries.lotteries.value.getValue(lotteryId)

    @Test
    fun drawsTwoWinnersAfterRegistrationEnds() = runBlocking {
        val result = controller.drawIfDue(lottery())

        assertNotNull(result)
        assertEquals(2, result!!.winners.size)
        assertEquals(Lottery.STATUS_DRAWN, lottery().status)
        val statuses = lotteries.waitingLists.value.getValue(lotteryId).groupingBy { it.status }.eachCount()
        assertEquals(2, statuses[WaitingListEntry.STATUS_WON])
        assertEquals(2, statuses[WaitingListEntry.STATUS_NOT_SELECTED])
    }

    @Test
    fun winnersAreNotified() = runBlocking {
        val result = controller.drawIfDue(lottery())!!

        assertEquals(result.winners.map { it.entrantId }, notifications.sent.map { it.recipientId })
        notifications.sent.forEach {
            assertEquals(AppNotification.TYPE_LOTTERY_WON, it.type)
            assertEquals("You won Jasper Riverside!", it.title)
            assertEquals(lotteryId, it.lotteryId)
            assertEquals(now, it.createdAt)
        }
    }

    @Test
    fun nothingHappensDuringRegistration() = runBlocking {
        now = TestData.DURING_REGISTRATION

        assertNull(controller.drawIfDue(lottery()))
        assertEquals(Lottery.STATUS_OPEN, lottery().status)
        assertTrue(notifications.sent.isEmpty())
    }

    @Test
    fun lotteryIsDrawnOnlyOnce() = runBlocking {
        val open = lottery()
        controller.drawIfDue(open)
        val sentAfterFirst = notifications.sent.size

        // A second device still holding the OPEN copy tries again.
        assertNull(controller.drawIfDue(open))
        assertEquals(sentAfterFirst, notifications.sent.size)
    }

    @Test
    fun drawAllDueSkipsLotteriesStillOpen() = runBlocking {
        val later = TestData.banffLottery.copy(registrationEnd = TestData.AFTER_REGISTRATION + 86_400_000)
        lotteries.lotteries.value = lotteries.lotteries.value + (later.id to later)

        assertEquals(1, controller.drawAllDue(lotteries.lotteries.value.values.toList()))
        assertEquals(Lottery.STATUS_OPEN, lotteries.lotteries.value.getValue(later.id).status)
    }
}
