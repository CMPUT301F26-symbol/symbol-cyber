/*
 * EntrantMessagesTest.kt
 * Unit tests for an entrant messaging the campsite owner about a
 * reservation they have (US 01.14.01).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeMessageRepository
import com.example.code.fakes.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EntrantMessagesTest {

    private val repository = FakeMessageRepository()
    private val controller = MessageController(repository) { TestData.AFTER_REGISTRATION }
    private val reservation = TestData.priyaReservation

    @Test
    fun entrantMessageGoesToTheOwner() = runBlocking {
        val message = controller.send(reservation, "device-priya-01", "Priya Shah", "Can we arrive at 9 pm?")

        assertEquals(TestData.OWNER_ID, message.recipientId)
        assertFalse(message.fromOwner)
        assertEquals("Priya Shah", message.senderName)
    }

    @Test
    fun entrantWithoutTheReservationCannotMessage() {
        assertFalse(MessageController.canMessage(reservation, "device-liam-02"))
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.send(reservation, "device-liam-02", "Liam Chen", "Hi") }
        }
        assertTrue(repository.sent.value.isEmpty())
    }

    @Test
    fun ownerAndEntrantShareOneConversationInOrder() = runBlocking {
        controller.send(reservation, "device-priya-01", "Priya Shah", "Is there firewood?")
        controller.send(reservation, TestData.OWNER_ID, "Sam", "Yes, at the gate.")

        val texts = controller.conversation(reservation).first().map { it.text }
        assertEquals(listOf("Is there firewood?", "Yes, at the gate."), texts)
    }
}
