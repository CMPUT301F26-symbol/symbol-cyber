/*
 * NotificationRepository.kt
 * Saves in-app notifications to Firestore (notifications/{id}).
 * Known issues: listing a user's notifications in the Notifications tab
 * belongs to US 01.03.01 and is not part of this file yet.
 */

package com.example.code.data

import com.example.code.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore

/** Access to in-app notifications. */
interface NotificationRepository {

    /** Saves every notification in [notifications] in one batch. */
    suspend fun sendAll(notifications: List<AppNotification>)
}

/** [NotificationRepository] backed by Cloud Firestore. */
class FirestoreNotificationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : NotificationRepository {

    override suspend fun sendAll(notifications: List<AppNotification>) {
        if (notifications.isEmpty()) return
        val collection = db.collection(NOTIFICATIONS)
        val batch = db.batch()
        notifications.forEach { notification ->
            val ref = collection.document()
            batch.set(ref, notificationToMap(notification.copy(id = ref.id)))
        }
        batch.commit().awaitResult()
    }

    companion object {
        /** Name of the notifications collection. */
        const val NOTIFICATIONS = "notifications"
    }
}

/** Converts [notification] to the fields stored in Firestore. */
fun notificationToMap(notification: AppNotification): Map<String, Any> = mapOf(
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
