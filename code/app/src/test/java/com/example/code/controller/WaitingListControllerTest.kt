/*
 * WaitingListControllerTest.kt
 * Unit tests for WaitingListController: which lottery a campsite's details
 * screen shows (US 01.01.06). Uses an in-memory fake repository.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeLotteryRepository
import com.example.code.fakes.TestData
import com.example.code.model.Lottery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WaitingListControllerTest {

    private val lotteries = FakeLotteryRepository(listOf(TestData.jasperLottery, TestData.banffLottery))
    private val controller = WaitingListController(lotteries)

    @Test
    fun lotteriesComeFromTheRepository() = runBlocking {
        val loaded = controller.allLotteries.first()
        assertEquals(setOf("lottery-jasper-nov", "lottery-banff-nov"), loaded.map { it.id }.toSet())
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
}
