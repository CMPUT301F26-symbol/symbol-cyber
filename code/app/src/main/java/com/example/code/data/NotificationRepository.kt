/*
 * NotificationRepository.kt
 * Saves and loads in-app notifications in Cloud Firestore.
 * Notification delivery respects each user's notification preference.
 * If a preference can't be read, the notification is still delivered.
 */

package com.example.code.data

import android.util.Log
import com.example.code.model.AppNotification
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Access to persisted application notifications.
 */
interface NotificationRepository {

    /**
     * Saves notifications for recipients who have notifications enabled.
     */
    suspend fun sendAll(notifications: List<AppNotification>)

    /**
     * Observes notifications belonging to [recipientId].
     */
    fun notificationsFor(recipientId: String): Flow<List<AppNotification>>

    /**
     * Observes notifications sent by [senderId], newest first (US 02.10.02).
     */
    fun notificationsSentBy(senderId: String): Flow<List<AppNotification>> = flowOf(emptyList())
}

/**
 * [NotificationRepository] backed by Cloud Firestore.
 */
class FirestoreNotificationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : NotificationRepository {

    override suspend fun sendAll(notifications: List<AppNotification>) {
        if (notifications.isEmpty()) {
            return
        }

        val deliverableNotifications =
            notifications.filter { notification ->
                // A failed read counts as enabled, so one bad read doesn't drop everyone's notifications
                val enabled =
                    try {
                        db.collection(USERS)
                            .document(notification.recipientId)
                            .get()
                            .awaitResult()
                            .getBoolean("notificationsEnabled")
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not read notification preference", e)
                        null
                    }

                shouldDeliverNotification(enabled)
            }

        if (deliverableNotifications.isEmpty()) {
            return
        }

        val collection = db.collection(NOTIFICATIONS)
        val batch = db.batch()

        deliverableNotifications.forEach { notification ->
            val ref = collection.document()

            batch.set(
                ref,
                notificationToMap(
                    notification.copy(id = ref.id),
                ),
            )
        }

        batch.commit().awaitResult()
    }

    override fun notificationsFor(
        recipientId: String,
    ): Flow<List<AppNotification>> =
        callbackFlow {
            val registration =
                db.collection(NOTIFICATIONS)
                    .whereEqualTo("recipientId", recipientId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val notifications =
                            snapshot
                                ?.documents
                                .orEmpty()
                                .map { document ->
                                    AppNotification(
                                        id =
                                            document.getString("id")
                                                ?: document.id,
                                        recipientId =
                                            document.getString("recipientId")
                                                ?: "",
                                        senderId =
                                            document.getString("senderId")
                                                ?: "",
                                        lotteryId =
                                            document.getString("lotteryId")
                                                ?: "",
                                        campsiteName =
                                            document.getString("campsiteName")
                                                ?: "",
                                        type =
                                            document.getString("type")
                                                ?: AppNotification.TYPE_WAITLIST_MESSAGE,
                                        title =
                                            document.getString("title")
                                                ?: "",
                                        message =
                                            document.getString("message")
                                                ?: "",
                                        createdAt =
                                            document.getLong("createdAt")
                                                ?: 0L,
                                    )
                                }
                                .sortedByDescending {
                                    it.createdAt
                                }

                        trySend(notifications)
                    }

            awaitClose {
                registration.remove()
            }
        }

    override fun notificationsSentBy(senderId: String): Flow<List<AppNotification>> =
        callbackFlow {
            val registration =
                db.collection(NOTIFICATIONS)
                    .whereEqualTo("senderId", senderId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        val notifications = snapshot?.documents.orEmpty()
                            .map(::sentNotificationFromDocument)
                            .sortedByDescending { it.createdAt }
                        trySend(notifications)
                    }
            awaitClose { registration.remove() }
        }

    companion object {
        const val NOTIFICATIONS = "notifications"
        private const val USERS = "users"
        private const val TAG = "NotificationRepository"
    }
    /**
     * Returns whether a notification should be delivered for the stored preference.
     *
     * Missing preferences default to enabled for existing users.
     */

}

/**
 * Converts [notification] to the fields stored in Firestore.
 */
fun notificationToMap(
    notification: AppNotification,
): Map<String, Any> =
    mapOf(
        "id" to notification.id,
        "recipientId" to notification.recipientId,
        "senderId" to notification.senderId,
        "lotteryId" to notification.lotteryId,
        "campsiteName" to notification.campsiteName,
        "type" to notification.type,
        "title" to notification.title,
        "message" to notification.message,
        "createdAt" to notification.createdAt,
    )

fun shouldDeliverNotification(
    notificationsEnabled: Boolean?,
): Boolean {
    return notificationsEnabled != false
}

/** Builds an [AppNotification] from a Firestore [document] (US 02.10.02). */
private fun sentNotificationFromDocument(document: DocumentSnapshot): AppNotification =
    AppNotification(
        id = document.getString("id") ?: document.id,
        recipientId = document.getString("recipientId") ?: "",
        senderId = document.getString("senderId") ?: "",
        lotteryId = document.getString("lotteryId") ?: "",
        campsiteName = document.getString("campsiteName") ?: "",
        type = document.getString("type") ?: AppNotification.TYPE_WAITLIST_MESSAGE,
        title = document.getString("title") ?: "",
        message = document.getString("message") ?: "",
        createdAt = document.getLong("createdAt") ?: 0L,
    )
