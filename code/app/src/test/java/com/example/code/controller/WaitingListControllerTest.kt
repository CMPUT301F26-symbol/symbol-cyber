/*
 * WaitingListControllerTest.kt
 * Unit tests for WaitingListController: which lottery a campsite's details
 * screen shows (US 01.01.06), joining and leaving the waiting list
 * (US 01.02.01, 01.02.02), the waitlist cap, the owner's lotteries
 * (US 02.09.01) and notifying the waiting list (US 02.10.01). Uses
 * in-memory fake repositories.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.data.JoinResult
import com.example.code.fakes.FakeLotteryRepository
import com.example.code.fakes.FakeNotificationRepository
import com.example.code.fakes.TestData
import com.example.code.model.AppNotification
import com.example.code.model.Lottery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WaitingListControllerTest {

    private val lotteries = FakeLotteryRepository(listOf(TestData.jasperLottery, TestData.banffLottery))
    private val notifications = FakeNotificationRepository()
    private var now = TestData.DURING_REGISTRATION
    private val controller = WaitingListController(lotteries, notifications) { now }

    private fun jasper(): Lottery = lotteries.lotteries.value.getValue(TestData.jasperLottery.id)

    @Test
    fun lotteriesComeFromTheRepository() = runBlocking {
        val loaded = controller.allLotteries.first()
        assertEquals(setOf("lottery-jasper-nov", "lottery-banff-nov"), loaded.map { it.id }.toSet())
    }

    @Test
    fun entrantJoinsDuringRegistration() = runBlocking {
        val result = controller.join(jasper(), TestData.priya.entrantId, "Priya Shah")

        assertEquals(JoinResult.JOINED, result)
        val list = controller.waitingList(TestData.jasperLottery.id).first()
        assertEquals(listOf("Priya Shah"), list.map { it.entrantName })
        assertEquals(now, list.single().joinedAt)
        assertEquals(1, jasper().waitingListCount)
    }

    @Test
    fun joiningTwiceIsReported() = runBlocking {
        controller.join(jasper(), TestData.priya.entrantId, "Priya Shah")
        val second = controller.join(jasper(), TestData.priya.entrantId, "Priya Shah")

        assertEquals(JoinResult.ALREADY_JOINED, second)
        assertEquals(1, jasper().waitingListCount)
    }

    @Test
    fun cannotJoinBeforeRegistrationOpens() = runBlocking {
        now = TestData.BEFORE_REGISTRATION
        assertEquals(JoinResult.CLOSED, controller.join(jasper(), TestData.liam.entrantId, "Liam Chen"))
    }

    @Test
    fun cannotJoinAfterRegistrationCloses() = runBlocking {
        now = TestData.AFTER_REGISTRATION
        assertEquals(JoinResult.CLOSED, controller.join(jasper(), TestData.liam.entrantId, "Liam Chen"))
    }

    @Test
    fun fifthEntrantIsRefusedWhenCapIsFour() = runBlocking {
        listOf(TestData.priya, TestData.liam, TestData.sofia, TestData.noah).forEach {
            assertEquals(JoinResult.JOINED, controller.join(jasper(), it.entrantId, it.entrantName))
        }
        val result = controller.join(jasper(), "device-emma-05", "Emma Roy")

        assertEquals(JoinResult.FULL, result)
        assertEquals(4, jasper().waitingListCount)
    }

    @Test
    fun blankEntrantIdIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.join(jasper(), " ", "No Device") }
        }
    }

    @Test
    fun entrantLeavesDuringRegistration() = runBlocking {
        controller.join(jasper(), TestData.priya.entrantId, "Priya Shah")

        assertTrue(controller.leave(jasper(), TestData.priya.entrantId))
        assertTrue(controller.waitingList(TestData.jasperLottery.id).first().isEmpty())
        assertEquals(0, jasper().waitingListCount)
    }

    @Test
    fun leavingWhenNotJoinedReturnsFalse() = runBlocking {
        assertFalse(controller.leave(jasper(), TestData.liam.entrantId))
    }

    @Test
    fun cannotLeaveAfterRegistrationCloses() = runBlocking {
        controller.join(jasper(), TestData.priya.entrantId, "Priya Shah")
        now = TestData.AFTER_REGISTRATION

        assertFalse(controller.leave(jasper(), TestData.priya.entrantId))
        assertEquals(1, jasper().waitingListCount)
    }

    @Test
    fun ownerNotifiesEveryEntrant() = runBlocking {
        val entries = listOf(TestData.priya, TestData.liam, TestData.sofia)

        val sent = controller.notifyWaitingList(
            jasper(),
            TestData.OWNER_ID,
            entries,
            "  Fire ban in effect this weekend.  ",
        )

        assertEquals(3, sent)
        assertEquals(entries.map { it.entrantId }, notifications.sent.map { it.recipientId })
        notifications.sent.forEach {
            assertEquals("Fire ban in effect this weekend.", it.message)
            assertEquals(TestData.OWNER_ID, it.senderId)
            assertEquals(TestData.jasperLottery.id, it.lotteryId)
            assertEquals(AppNotification.TYPE_WAITLIST_MESSAGE, it.type)
            assertEquals("Message about Jasper Riverside", it.title)
        }
    }

    @Test
    fun blankMessageIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                controller.notifyWaitingList(jasper(), TestData.OWNER_ID, listOf(TestData.priya), "   ")
            }
        }
        assertTrue(notifications.sent.isEmpty())
    }

    @Test
    fun detailsShowTheCampsitesOpenLottery() {
        val old = TestData.jasperLottery.copy(id = "lottery-jasper-oct", status = Lottery.STATUS_DRAWN)
        val all = listOf(old, TestData.jasperLottery, TestData.banffLottery)

        assertEquals(
            TestData.jasperLottery,
            WaitingListController.lotteryForCampsite(all, "camp-jasper-riverside"),
        )
        assertNull(WaitingListController.lotteryForCampsite(all, "camp-elk-island"))
    }

    @Test
    fun detailsShowLatestLotteryWhenNoneIsOpen() {
        val older = TestData.jasperLottery.copy(id = "a", status = Lottery.STATUS_DRAWN)
        val newer = older.copy(id = "b", registrationEnd = older.registrationEnd + 86_400_000)

        val shown = WaitingListController.lotteryForCampsite(listOf(older, newer), "camp-jasper-riverside")
        assertEquals(newer, shown)
    }

    @Test
    fun ownerSeesOnlyTheirLotteries() {
        val other = TestData.banffLottery.copy(id = "lottery-other", ownerId = "someone-else")
        val all = listOf(TestData.jasperLottery, TestData.banffLottery, other)

        val mine = WaitingListController.lotteriesOfOwner(all, TestData.OWNER_ID)

        assertEquals(setOf("lottery-jasper-nov", "lottery-banff-nov"), mine.map { it.id }.toSet())
    }
}
