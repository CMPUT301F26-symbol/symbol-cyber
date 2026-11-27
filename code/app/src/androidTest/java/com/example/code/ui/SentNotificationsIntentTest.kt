/*
 * SentNotificationsIntentTest.kt
 * UI (intent) tests for the owner's list of sent notifications
 * (US 02.10.02). Screens get canned data, so no Firestore is needed.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.controller.SentNotification
import com.example.code.model.AppNotification
import com.example.code.ui.common.formatDateTime
import com.example.code.ui.owner.OwnerLotteriesScreen
import com.example.code.ui.owner.SentNotificationsScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SentNotificationsIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sentAt = 1794232800000L

    @Test
    fun ownerSeesSentNotifications() {
        composeRule.setContent {
            SentNotificationsScreen(
                sent = listOf(
                    SentNotification("Jasper Riverside", "Jasper Riverside", "Bring bear spray", AppNotification.TYPE_WAITLIST_MESSAGE, sentAt, 3),
                ),
                onBack = {},
            )
        }

        composeRule.onNodeWithText("Sent notifications").assertExists()
        composeRule.onNodeWithText("Bring bear spray").assertExists()
        composeRule.onNodeWithText("Sent ${formatDateTime(sentAt)} to 3 entrants").assertExists()
    }

    @Test
    fun ownerWithoutSentNotificationsSeesMessage() {
        composeRule.setContent {
            SentNotificationsScreen(sent = emptyList(), onBack = {})
        }

        composeRule.onNodeWithText("You haven't sent any notifications yet.").assertExists()
    }

    @Test
    fun ownerOpensSentNotificationsFromLotteries() {
        var opened = false
        composeRule.setContent {
            OwnerLotteriesScreen(lotteries = emptyList(), onOpenWaitingList = {}, onOpenSentNotifications = { opened = true })
        }

        composeRule.onNodeWithText("Sent notifications").performClick()
        assertTrue(opened)
    }
}
