/*
 * NotificationRoutes.kt
 * Connects entrant notification data from Firestore to the Notifications UI.
 */

package com.example.code.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.code.data.NotificationRepository
import com.example.code.ui.entrant.EntrantNotificationsScreen

/**
 * Displays live notifications for [deviceId].
 *
 * @param deviceId current entrant's device identifier.
 * @param repository notification data source.
 * @param onOpenReservations opens the entrant reservation screen.
 */
@Composable
fun EntrantNotificationsRoute(
    deviceId: String,
    repository: NotificationRepository,
    onOpenReservations: () -> Unit,
) {
    val notificationFlow =
        remember(deviceId, repository) {
            repository.notificationsFor(deviceId)
        }

    val notifications by
    notificationFlow.collectAsState(
        initial = emptyList(),
    )

    EntrantNotificationsScreen(
        notifications = notifications,
        onOpenReservations = onOpenReservations,
    )
}

/**
 * Owner's list of sent notifications (US 02.10.02).
 *
 * @param deviceId this owner's ID, the sender.
 * @param repository where notifications are read.
 * @param onBack called when the owner goes back.
 */
@Composable
fun SentNotificationsRoute(
    deviceId: String,
    repository: NotificationRepository,
    onBack: () -> Unit,
) {
    val sent by remember(deviceId) { repository.notificationsSentBy(deviceId) }
        .collectAsState(initial = emptyList())
    com.example.code.ui.owner.SentNotificationsScreen(
        sent = com.example.code.controller.SentNotifications.group(sent),
        onBack = onBack,
    )
}
