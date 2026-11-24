/* Maps notification and reservation-message documents into the admin log without changing delivery. */
package com.example.code.admin

import com.example.code.data.FirestoreNotificationRepository
import com.example.code.data.awaitResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/** Protected reads use the server so an old, cached result is never shown after access is denied. */
class FirestoreAdminLogs(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    suspend fun notifications(): List<AdminLogEntry> =
        db.collection(FirestoreNotificationRepository.NOTIFICATIONS).get(Source.SERVER)
            .awaitResult().documents.map { document ->
                AdminLogEntry(
                    id = document.id,
                    title = document.getString("title").orEmpty().ifBlank { "Notification" },
                    body = document.getString("message").orEmpty(),
                    sender = document.getString("senderId").orEmpty().ifBlank { "System" },
                    recipient = document.getString("recipientId").orEmpty(),
                    context = listOfNotNull(
                        document.getString("type"),
                        document.getString("campsiteName")?.takeIf { it.isNotBlank() },
                        document.getString("lotteryId")?.takeIf { it.isNotBlank() }?.let { "Lottery: ${it}" },
                    ).joinToString(" · "),
                    sentAt = document.getLong("createdAt")?.takeIf { it > 0 },
                )
            }
    /** Reads owner messages stored by the reservation messaging feature (US 02.12.01). */
    suspend fun reservationMessages(): List<AdminLogEntry> =
        db.collection("reservationMessages").get(Source.SERVER)
            .awaitResult().documents.map { document ->
                AdminLogEntry(
                    id = document.id,
                    title = document.getString("campsiteName").orEmpty().ifBlank { "Reservation message" },
                    body = document.getString("message").orEmpty(),
                    sender = document.getString("ownerId").orEmpty(),
                    recipient = document.getString("entrantId").orEmpty(),
                    context = document.getString("reservationId").orEmpty()
                        .takeIf { it.isNotBlank() }?.let { "Reservation: ${it}" }.orEmpty(),
                    sentAt = when (val createdAt = document.get("createdAt")) {
                        is com.google.firebase.Timestamp -> createdAt.toDate().time
                        is Number -> createdAt.toLong().takeIf { it > 0 }
                        else -> null
                    },
                )
            }

}
