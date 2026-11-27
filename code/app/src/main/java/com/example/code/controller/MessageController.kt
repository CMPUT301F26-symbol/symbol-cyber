/*
 * MessageController.kt
 * Controller for messages about a reservation: the campsite owner messages
 * the entrant who reserved (US 02.12.01) and the entrant messages the owner
 * (US 01.14.01). Only the two people on the reservation can message.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.data.MessageRepository
import com.example.code.model.Message
import com.example.code.model.Reservation
import kotlinx.coroutines.flow.Flow

/**
 * Sends and loads messages between an owner and an entrant.
 *
 * @param messages where messages are read and saved.
 * @param clock current time in milliseconds; replaceable in tests.
 */
class MessageController(
    private val messages: MessageRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    /** Messages about [reservation], oldest first. */
    fun conversation(reservation: Reservation): Flow<List<Message>> =
        messages.observeConversation(reservation.id)

    /**
     * Sends [text] from [senderId] about [reservation] to the other person.
     *
     * @param senderName name shown with the message.
     * @return the message that was sent.
     * @throws IllegalArgumentException if the text is blank or too long, the
     * sender is not on the reservation, or the reservation is not active.
     */
    suspend fun send(reservation: Reservation, senderId: String, senderName: String, text: String): Message {
        val body = text.trim()
        require(body.isNotEmpty()) { "Write a message first" }
        require(body.length <= MAX_LENGTH) { "Messages can be at most $MAX_LENGTH characters" }
        require(canMessage(reservation, senderId)) { "Only the owner and the entrant can message here" }
        require(reservation.status == Reservation.STATUS_ACTIVE) { "This reservation is no longer active" }

        val fromOwner = senderId == reservation.ownerId
        val message = Message(
            reservationId = reservation.id,
            campsiteName = reservation.campsiteName,
            senderId = senderId,
            senderName = senderName.trim(),
            recipientId = if (fromOwner) reservation.entrantId else reservation.ownerId,
            fromOwner = fromOwner,
            text = body,
            sentAt = clock(),
        )
        messages.send(message)
        return message
    }

    companion object {
        /** Longest message allowed. */
        const val MAX_LENGTH = 500

        /** Returns true if [userId] is the owner or the entrant of [reservation]. */
        fun canMessage(reservation: Reservation, userId: String): Boolean =
            userId.isNotBlank() && (userId == reservation.ownerId || userId == reservation.entrantId)
    }
}
