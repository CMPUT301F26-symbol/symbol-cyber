/*
 * ConversationScreen.kt
 * Messages between a campsite owner and an entrant about one reservation
 * (US 02.12.01, US 01.14.01). The user's own messages are on the right.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.code.model.Message

/**
 * Shows a conversation and a box to write a new message.
 *
 * @param title heading, e.g. the campsite and the other person.
 * @param messages messages, oldest first.
 * @param myId this user's device ID.
 * @param canSend false hides the box, e.g. for a cancelled reservation.
 * @param busy true while a message is being sent.
 * @param errorMessage result of the last failed send, or null.
 * @param onSend called with the text to send; returns nothing.
 * @param onBack goes back.
 */
@Composable
fun ConversationScreen(
    title: String,
    messages: List<Message>,
    myId: String,
    canSend: Boolean,
    busy: Boolean,
    errorMessage: String?,
    onSend: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = modifier.fillMaxSize().imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBack, modifier = Modifier.testTag("conversationBack")) { Text("Back") }
        Text(title, style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (messages.isEmpty()) {
                item { Text("No messages yet. Say hello!") }
            }
            items(messages, key = { it.id.ifBlank { "${it.sentAt}-${it.senderId}" } }) { message ->
                MessageBubble(message, mine = message.senderId == myId)
            }
        }

        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("messageError"))
        }

        if (canSend) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("Message") },
                    modifier = Modifier.weight(1f).testTag("messageInput"),
                )
                Button(
                    onClick = {
                        onSend(draft)
                        draft = ""
                    },
                    enabled = !busy && draft.isNotBlank(),
                    modifier = Modifier.testTag("sendMessage"),
                ) { Text("Send") }
            }
        } else {
            Text("This reservation is no longer active, so no new messages can be sent.")
        }
    }
}

/** One message, on the right if [mine]. */
@Composable
private fun MessageBubble(message: Message, mine: Boolean) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (mine) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    if (mine) "You" else message.senderName.ifBlank { if (message.fromOwner) "Owner" else "Entrant" },
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(message.text)
                Text(formatDateTime(message.sentAt), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
