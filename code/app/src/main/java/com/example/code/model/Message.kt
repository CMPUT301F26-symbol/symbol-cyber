/*
 * Message.kt
 * A chat message between a campsite owner and an entrant about one
 * reservation (Firestore: messages/{id}), US 02.12.01 and US 01.14.01.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.model

/**
 * One message about a reservation.
 *
 * @property id Firestore document ID.
 * @property reservationId reservation the conversation is about.
 * @property campsiteName campsite name, for display.
 * @property senderId device ID of the sender.
 * @property senderName name shown next to the message.
 * @property recipientId device ID of the other person.
 * @property fromOwner true if the campsite owner sent it.
 * @property text message text.
 * @property sentAt time sent, in milliseconds since the epoch.
 */
data class Message(
    val id: String = "",
    val reservationId: String = "",
    val campsiteName: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val fromOwner: Boolean = false,
    val text: String = "",
    val sentAt: Long = 0L,
)
