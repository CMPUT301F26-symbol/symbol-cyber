/*
 * SentNotificationsTest.kt
 * Unit tests for the owner's list of sent notifications (US 02.10.02).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeNotificationRepository
import com.example.code.fakes.TestData
import com.example.code.model.AppNotification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SentNotificationsTest {

    private fun note(recipient: String, message: String, at: Long, sender: String = TestData.OWNER_ID) = AppNotification(
        recipientId = recipient,
        senderId = sender,
        lotteryId = "lottery-jasper-nov",
        campsiteName = "Jasper Riverside",
        title = "Jasper Riverside",
        message = message,
        createdAt = at,
    )

    @Test
    fun oneMessageToManyEntrantsIsShownOnce() {
        val sent = listOf(note("a", "Bring bear spray", 10L), note("b", "Bring bear spray", 10L), note("c", "Bring bear spray", 10L))

        val grouped = SentNotifications.group(sent)

        assertEquals(1, grouped.size)
        assertEquals(3, grouped.single().recipientCount)
        assertEquals("Bring bear spray", grouped.single().message)
    }

    @Test
    fun differentMessagesAreNewestFirst() {
        val sent = listOf(note("a", "First", 10L), note("a", "Second", 20L))

        assertEquals(listOf("Second", "First"), SentNotifications.group(sent).map { it.message })
    }

    @Test
    fun repositoryReturnsOnlyThisOwnersNotifications() = runBlocking {
        val repository = FakeNotificationRepository()
        repository.sendAll(listOf(note("a", "Mine", 10L), note("a", "Not mine", 11L, sender = "another-owner")))

        val mine = repository.notificationsSentBy(TestData.OWNER_ID).first()

        assertEquals(listOf("Mine"), mine.map { it.message })
    }

    @Test
    fun typesHaveReadableLabels() {
        assertEquals("Lottery result", SentNotifications.typeLabel(AppNotification.TYPE_LOTTERY_WON))
        assertEquals("Message to waiting list", SentNotifications.typeLabel(AppNotification.TYPE_WAITLIST_MESSAGE))
    }
}
