/*
 * SentNotificationsScreen.kt
 * List of all notifications the campsite owner has sent (US 02.10.02).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.controller.SentNotification
import com.example.code.controller.SentNotifications
import com.example.code.ui.common.formatDateTime

/**
 * Shows the owner's sent notifications, newest first.
 *
 * @param sent grouped sent notifications.
 * @param onBack called when the owner goes back.
 */
@Composable
fun SentNotificationsScreen(
    sent: List<SentNotification>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { TextButton(onClick = onBack) { Text("Back") } }
        item { Text("Sent notifications", style = MaterialTheme.typography.headlineSmall) }
        if (sent.isEmpty()) {
            item { Text("You haven't sent any notifications yet.") }
        }
        items(sent, key = { "${it.sentAt}-${it.title}-${it.campsiteName}" }) { notification ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(notification.title.ifBlank { notification.campsiteName }, style = MaterialTheme.typography.titleSmall)
                    Text(notification.message)
                    Text(
                        "${SentNotifications.typeLabel(notification.type)} · ${notification.campsiteName}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "Sent ${formatDateTime(notification.sentAt)} to ${notification.recipientCount} " +
                                if (notification.recipientCount == 1) "entrant" else "entrants",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
