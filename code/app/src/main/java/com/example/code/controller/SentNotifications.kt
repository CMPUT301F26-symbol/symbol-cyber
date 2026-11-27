/*
 * SentNotifications.kt
 * Groups the notifications a campsite owner has sent (US 02.10.02): one
 * notification sent to many entrants is shown once, with how many got it.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.AppNotification

/**
 * One notification the owner sent, possibly to several entrants.
 *
 * @property campsiteName campsite of the lottery it was about.
 * @property title notification title.
 * @property message notification text.
 * @property type [AppNotification] type.
 * @property sentAt time sent.
 * @property recipientCount how many entrants received it.
 */
data class SentNotification(
    val campsiteName: String,
    val title: String,
    val message: String,
    val type: String,
    val sentAt: Long,
    val recipientCount: Int,
)

/** Builds the owner's sent notifications list. */
object SentNotifications {

    /** Groups [sent] by what was sent and when, newest first. */
    fun group(sent: List<AppNotification>): List<SentNotification> =
        sent.groupBy { listOf(it.lotteryId, it.type, it.title, it.message, it.createdAt.toString()) }
            .map { (_, copies) ->
                val first = copies.first()
                SentNotification(
                    campsiteName = first.campsiteName,
                    title = first.title,
                    message = first.message,
                    type = first.type,
                    sentAt = first.createdAt,
                    recipientCount = copies.map { it.recipientId }.distinct().size,
                )
            }
            .sortedByDescending { it.sentAt }

    /** Short label for a notification [type]. */
    fun typeLabel(type: String): String = when (type) {
        AppNotification.TYPE_LOTTERY_WON -> "Lottery result"
        else -> "Message to waiting list"
    }
}
