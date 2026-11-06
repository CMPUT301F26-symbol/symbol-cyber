/*
 * AppNotification.kt
 * Model of an in-app notification stored in Firestore (notifications/{id})
 * and shown to the recipient in the Notifications tab.
 * Known issues: notifications are in-app only; there are no push notifications.
 */

package com.example.code.model

/**
 * A notification sent to one entrant.
 *
 * Named AppNotification to avoid a clash with android.app.Notification.
 *
 * @property id Firestore document ID.
 * @property recipientId device ID of the entrant who receives it.
 * @property senderId device ID of the sender, or empty if the system sent it.
 * @property lotteryId lottery the notification is about.
 * @property campsiteName campsite name, for display.
 * @property type [TYPE_WAITLIST_MESSAGE] or [TYPE_LOTTERY_WON].
 * @property title short heading.
 * @property message body text.
 * @property createdAt time sent, in milliseconds since the epoch.
 */
data class AppNotification(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val lotteryId: String = "",
    val campsiteName: String = "",
    val type: String = TYPE_WAITLIST_MESSAGE,
    val title: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
) {
    companion object {
        /** Message from a campsite owner to their waiting list (US 02.10.01). */
        const val TYPE_WAITLIST_MESSAGE = "WAITLIST_MESSAGE"

        /** Entrant was drawn as a lottery winner (US 01.03.01). */
        const val TYPE_LOTTERY_WON = "LOTTERY_WON"
    }
}
