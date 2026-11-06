/*
 * NotificationIntentTest.kt
 * UI tests for US 01.03.01 winner notifications.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.AppNotification
import com.example.code.ui.entrant.EntrantNotificationsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val winnerNotification =
        AppNotification(
            id = "notification-1",
            recipientId = "device-123",
            lotteryId = "lottery-123",
            campsiteName = "Jasper Riverside",
            type = AppNotification.TYPE_LOTTERY_WON,
            title = "You won Jasper Riverside!",
            message = "Open the campsite to accept your reservation.",
            createdAt = 1000L,
        )

    @Test
    fun winnerNotificationIsDisplayed() {
        composeRule.setContent {
            EntrantNotificationsScreen(
                notifications = listOf(winnerNotification),
                onOpenReservations = {},
            )
        }

        composeRule
            .onNodeWithText("You won Jasper Riverside!")
            .assertExists()

        composeRule
            .onNodeWithText(
                "Open the campsite to accept your reservation.",
            )
            .assertExists()

        composeRule
            .onNodeWithText("View reservations")
            .assertExists()
    }

    @Test
    fun winnerCanOpenReservations() {
        var openedReservations = false

        composeRule.setContent {
            EntrantNotificationsScreen(
                notifications = listOf(winnerNotification),
                onOpenReservations = {
                    openedReservations = true
                },
            )
        }

        composeRule
            .onNodeWithText("View reservations")
            .performClick()

        assertTrue(openedReservations)
    }

    @Test
    fun emptyNotificationListShowsMessage() {
        composeRule.setContent {
            EntrantNotificationsScreen(
                notifications = emptyList(),
                onOpenReservations = {},
            )
        }

        composeRule
            .onNodeWithText("You have no notifications.")
            .assertExists()
    }
}