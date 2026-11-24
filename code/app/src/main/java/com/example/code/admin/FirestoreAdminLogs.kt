/* Maps existing notification documents into the admin log without changing delivery. */
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
}
