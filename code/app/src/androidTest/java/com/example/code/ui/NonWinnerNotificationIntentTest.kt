/*
 * NonWinnerNotificationIntentTest.kt
 * US 01.03.02: verifies actual draw-generated non-winner messages in the notification UI.
 * Persistence is replaced by test repositories; no shared Firestore data is changed.
 */
package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.controller.DrawController
import com.example.code.model.AppNotification
import com.example.code.ui.entrant.EntrantNotificationsScreen
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class NonWinnerNotificationIntentTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun nonWinnerSeesOutcomeWithoutReservationAction() = runBlocking {
        val data = LotteryUiFixtures
        val repository = LotteryDrawUiRepository(data.lottery, data.entrants)
        val notifications = RecordingDrawNotifications()
        val controller = DrawController(
            repository,
            notifications,
            clock = { data.registrationEnd.millis() },
            random = Random(17),
        )
        val result = controller.drawIfDue(data.lottery)!!
        val entrantId = result.notSelected.single().entrantId
        val received = notifications.sent.filter { it.recipientId == entrantId }
        assertEquals(1, received.size)
        assertEquals(AppNotification.TYPE_LOTTERY_NOT_SELECTED, received.single().type)

        compose.setContent {
            MaterialTheme {
                EntrantNotificationsScreen(
                    notifications = received.mapIndexed { index, item -> item.copy(id = "notification-$index") },
                    onOpenReservations = { error("Non-winners cannot reserve from a notification") },
                )
            }
        }
        compose.onNodeWithText("Lottery result: ${data.campsite.name}").assertIsDisplayed()
        compose.onNodeWithText(
            "You were not selected this time. You can explore other campsites and lotteries.",
        ).assertIsDisplayed()
        compose.onNodeWithText("View reservations").assertDoesNotExist()
    }
}
