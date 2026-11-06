package com.example.code.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.code.controller.DrawController
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.example.code.navigation.AutomaticDraw
import com.example.code.ui.entrant.LotterySection
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/** AI USAGE
 * The basis for the following UI intent test bench file was generated using
 * GPT6-Sol "Please create a basic set of UI intent tests for my lottery files", 11-05-2026
 *
 */

/**
 * US 02.05.01: Instrumented tests of the existing automatic draw and result UI.
 * The clock is fixed, so tests do not wait for registration or the minute timer.
 * The actual AutomaticDraw, DrawController, and LotterySection are exercised;
 * only persistence and notification delivery are replaced by test fakes.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LotteryAutomaticDrawUiTest {
    @get:Rule
    val compose = createComposeRule()

    private val data = LotteryUiFixtures

    // US 02.05.01: A due lottery draws exactly N and displays the entrant's result.
    @Test
    fun dueLotteryDrawsTwoWinnersAndUpdatesEntrantFeedback() {
        val repository = LotteryDrawUiRepository(data.lottery, data.entrants)
        val notifications = RecordingDrawNotifications()
        showLottery(repository, notifications, now = data.registrationEnd.millis())
        waitForDraw(repository)

        var feedback = ""
        compose.runOnIdle {
            val entries = repository.waitingList.value
            assertEquals(2, entries.count { it.status == WaitingListEntry.STATUS_WON })
            assertEquals(1, entries.count { it.status == WaitingListEntry.STATUS_NOT_SELECTED })
            assertEquals(1, repository.completedDraws)
            assertEquals(2, notifications.sent.size)
            val winnerIds = entries.filter { it.status == WaitingListEntry.STATUS_WON }
                .map { it.entrantId }.toSet()
            assertEquals(winnerIds, notifications.sent.map { it.recipientId }.toSet())
            val myEntry = entries.first { it.entrantId == data.entrants.first().entrantId }
            feedback = if (myEntry.status == WaitingListEntry.STATUS_WON) {
                "You won this lottery!"
            } else {
                "You were not selected this time."
            }
        }
        compose.onNodeWithText(feedback).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Winners drawn: 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Waiting list: 3 of 10 spots taken").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Join Waiting List").assertDoesNotExist()
    }

    // US 02.05.01: If fewer than N entrants join, all are selected.
    @Test
    fun fewerEntrantsThanRequestedWinnersAllWin() {
        val entrants = data.entrants.take(2)
        val lottery = data.lottery.copy(winnerCount = 5, waitingListCount = entrants.size)
        val repository = LotteryDrawUiRepository(lottery, entrants)
        val notifications = RecordingDrawNotifications()
        showLottery(repository, notifications, now = data.registrationEnd.millis() + 60_000)
        waitForDraw(repository)
        compose.runOnIdle {
            assertTrue(repository.waitingList.value.all { it.status == WaitingListEntry.STATUS_WON })
            assertEquals(2, notifications.sent.size)
            assertEquals(1, repository.completedDraws)
        }
        compose.onNodeWithText("You won this lottery!").performScrollTo().assertIsDisplayed()
    }

    // US 02.04.03 / US 02.05.01: The automatic draw must not run before closing.
    @Test
    fun registrationStillOpenDoesNotDrawWinners() {
        val repository = LotteryDrawUiRepository(data.lottery, data.entrants)
        val notifications = RecordingDrawNotifications()
        showLottery(repository, notifications, now = data.registrationEnd.millis() - 60_000)
        compose.onNodeWithText("You are on the waiting list.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(Lottery.STATUS_OPEN, repository.lottery.value.status)
            assertEquals(0, repository.completedDraws)
            assertTrue(repository.waitingList.value.all { it.status == WaitingListEntry.STATUS_WAITING })
            assertTrue(notifications.sent.isEmpty())
        }
    }

    private fun showLottery(
        repository: LotteryDrawUiRepository,
        notifications: RecordingDrawNotifications,
        now: Long,
    ) {
        val controller = DrawController(
            lotteries = repository,
            notifications = notifications,
            clock = { now },
            random = Random(17),
        )
        compose.setContent {
            val lottery by repository.lottery.collectAsState()
            val entries by repository.waitingList.collectAsState()
            MaterialTheme {
                AutomaticDraw(lotteries = listOf(lottery), controller = controller)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    LotterySection(
                        lottery = lottery,
                        myEntry = entries.firstOrNull { it.entrantId == data.entrants.first().entrantId },
                        now = now,
                        busy = false,
                        statusMessage = null,
                        onJoin = {},
                        onLeave = {},
                    )
                }
            }
        }
    }

    private fun waitForDraw(repository: LotteryDrawUiRepository) {
        compose.waitUntil(timeoutMillis = 5_000) {
            repository.lottery.value.status == Lottery.STATUS_DRAWN
        }
        compose.waitForIdle()
    }
}
