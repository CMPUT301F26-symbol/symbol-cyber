/*
 * OwnerMessagesTest.kt
 * Unit tests for a campsite owner messaging the entrant who reserved
 * (US 02.12.01).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeMessageRepository
import com.example.code.fakes.TestData
import com.example.code.model.Reservation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerMessagesTest {

    private val repository = FakeMessageRepository()
    private val controller = MessageController(repository) { TestData.AFTER_REGISTRATION }
    private val reservation = TestData.priyaReservation

    @Test
    fun ownerMessageGoesToTheEntrant() = runBlocking {
        val message = controller.send(reservation, TestData.OWNER_ID, "Sam", "  Gate code is 4521  ")

        assertEquals("device-priya-01", message.recipientId)
        assertTrue(message.fromOwner)
        assertEquals("Gate code is 4521", message.text)
        assertEquals(TestData.AFTER_REGISTRATION, message.sentAt)
        assertEquals(listOf("Gate code is 4521"), controller.conversation(reservation).first().map { it.text })
    }

    @Test
    fun blankMessageIsNotSent() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.send(reservation, TestData.OWNER_ID, "Sam", "   ") }
        }
        assertTrue(repository.sent.value.isEmpty())
    }

    @Test
    fun tooLongMessageIsNotSent() {
        val text = "a".repeat(MessageController.MAX_LENGTH + 1)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.send(reservation, TestData.OWNER_ID, "Sam", text) }
        }
    }

    @Test
    fun anotherOwnerCannotMessage() {
        assertFalse(MessageController.canMessage(reservation, "another-owner"))
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.send(reservation, "another-owner", "Eve", "Hi") }
        }
    }

    @Test
    fun cannotMessageAboutARejectedReservation() {
        val rejected = reservation.copy(status = Reservation.STATUS_REJECTED)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.send(rejected, TestData.OWNER_ID, "Sam", "Hi") }
        }
    }

    @Test
    fun conversationsAreKeptPerReservation() = runBlocking {
        controller.send(reservation, TestData.OWNER_ID, "Sam", "For Priya")
        controller.send(TestData.liamReservation, TestData.OWNER_ID, "Sam", "For Liam")

        assertEquals(listOf("For Priya"), controller.conversation(reservation).first().map { it.text })
    }
}
