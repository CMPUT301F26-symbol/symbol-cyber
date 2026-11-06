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