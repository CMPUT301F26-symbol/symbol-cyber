/*
 * DrawRulesTest.kt
 * Unit tests for DrawController's draw rules (US 02.05.01): when a lottery
 * is due and how winners are picked.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.TestData
import com.example.code.model.Lottery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DrawRulesTest {

    private val lottery = TestData.jasperLottery
    private val entries = listOf(TestData.priya, TestData.liam, TestData.sofia, TestData.noah)

    @Test
    fun notDueDuringRegistration() {
        assertFalse(DrawController.isDue(lottery, TestData.DURING_REGISTRATION))
    }

    @Test
    fun dueOnceRegistrationEnds() {
        assertTrue(DrawController.isDue(lottery, TestData.REGISTRATION_END))
        assertTrue(DrawController.isDue(lottery, TestData.AFTER_REGISTRATION))
    }

    @Test
    fun notDueOnceDrawn() {
        val drawn = lottery.copy(status = Lottery.STATUS_DRAWN)
        assertFalse(DrawController.isDue(drawn, TestData.AFTER_REGISTRATION))
    }

    @Test
    fun lotteryWithoutEndTimeIsNeverDue() {
        assertFalse(DrawController.isDue(lottery.copy(registrationEnd = 0L), TestData.AFTER_REGISTRATION))
    }

    @Test
    fun picksExactlyWinnerCountAndSplitsEveryone() {
        val result = DrawController.pickWinners(entries, 2, Random(42))

        assertEquals(2, result.winners.size)
        assertEquals(2, result.notSelected.size)
        assertEquals(
            entries.map { it.entrantId }.toSet(),
            (result.winners + result.notSelected).map { it.entrantId }.toSet(),
        )
    }

    @Test
    fun sameSeedGivesSameWinners() {
        val first = DrawController.pickWinners(entries, 2, Random(7))
        val second = DrawController.pickWinners(entries, 2, Random(7))
        assertEquals(first, second)
    }

    @Test
    fun everyoneWinsWhenFewerEntrantsThanWinners() {
        val result = DrawController.pickWinners(listOf(TestData.priya), 2, Random(1))

        assertEquals(listOf(TestData.priya), result.winners)
        assertTrue(result.notSelected.isEmpty())
    }

    @Test
    fun emptyWaitingListHasNoWinners() {
        val result = DrawController.pickWinners(emptyList(), 2, Random(1))
        assertTrue(result.winners.isEmpty())
    }

    @Test
    fun zeroWinnersIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            DrawController.pickWinners(entries, 0)
        }
    }
}
