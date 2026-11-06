/*
 * OwnerWaitingListScreen.kt
 * Owner's view of one lottery's waiting list (mockup O05): who joined
 * (US 02.09.01) and a dialog to notify all of them (US 02.10.01, mockup O07).
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.controller.LotteryController
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.example.code.ui.common.formatDateTime

/**
 * Shows the entrants on [lottery]'s waiting list.
 *
 * @param lottery the lottery.
 * @param entries entrants on its waiting list.
 * @param statusMessage result of the last notification, or null.
 * @param onNotify called with the message text when the owner taps "Send".
 * @param onBack called when the owner taps "Back".
 */
@Composable
fun OwnerWaitingListScreen(
    lottery: Lottery,
    entries: List<WaitingListEntry>,
    statusMessage: String?,
    onNotify: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Waiting list: ${lottery.campsiteName}", style = MaterialTheme.typography.headlineSmall)
        Text("${entries.size} entrants joined")

        Button(
            onClick = { showDialog = true },
            enabled = entries.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Notify waiting list")
        }

        if (statusMessage != null) {
            Text(statusMessage, style = MaterialTheme.typography.bodySmall)
        }

        if (entries.isEmpty()) {
            Text("No entrants have joined yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.entrantId }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(LotteryController.entrantDisplayName(entry), style = MaterialTheme.typography.titleSmall)
                            Text("Joined ${formatDateTime(entry.joinedAt)}")
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        NotifyDialog(
            entrantCount = entries.size,
            onSend = { message ->
                onNotify(message)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}

/** Dialog where the owner types the message for the waiting list. */
@Composable
private fun NotifyDialog(
    entrantCount: Int,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var message by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notify $entrantCount entrants") },
        text = {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
            )
        },
        confirmButton = {
            TextButton(onClick = { onSend(message) }, enabled = message.isNotBlank()) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
