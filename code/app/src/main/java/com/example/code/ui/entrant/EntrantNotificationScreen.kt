/*
 * EntrantNotificationsScreen.kt
 * Displays notifications sent to the current entrant.
 * Lottery-win notifications provide a way to continue to reservations.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.AppNotification

/**
 * Displays notifications belonging to the current entrant.
 *
 * @param notifications notifications for this entrant, newest first.
 * @param onOpenReservations called when a lottery winner chooses to continue
 * to the reservation flow.
 */
@Composable
fun EntrantNotificationsScreen(
    notifications: List<AppNotification>,
    onOpenReservations: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineSmall,
        )

        if (notifications.isEmpty()) {
            Text("You have no notifications.")
            return@Column
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = notifications,
                key = { it.id },
            ) { notification ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(notification.message)

                        if (
                            notification.type ==
                            AppNotification.TYPE_LOTTERY_WON
                        ) {
                            Button(
                                onClick = onOpenReservations,
                            ) {
                                Text("View reservations")
                            }
                        }
                    }
                }
            }
        }
    }
}