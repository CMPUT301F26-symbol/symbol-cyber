/* Shows authorized audit records with refresh, empty and error states. */
package com.example.code.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.admin.AdminLogController
import com.example.code.admin.AdminLogEntry
import com.example.code.admin.AdminLogResult
import com.example.code.admin.FirebaseAdminAuthorization
import com.example.code.admin.FirestoreAdminLogs
import java.text.DateFormat
import java.util.Date

/** Connects the Admin Logs tab to its protected notification source. */
@Composable
fun AdminLogsRoute() {
    val repository = remember { FirestoreAdminLogs() }
    val controller = remember { AdminLogController(FirebaseAdminAuthorization()) }
    val notifications = remember(repository) { repository::notifications }
    val messages = remember(repository) { repository::reservationMessages }
    AdminLogsScreen(controller, notifications, messages)
}

@Composable
internal fun AdminLogsScreen(
    controller: AdminLogController,
    readNotifications: suspend () -> List<AdminLogEntry>,
    readMessages: suspend () -> List<AdminLogEntry>,
) {
    var showMessages by rememberSaveable { mutableStateOf(false) }
    val read = if (showMessages) readMessages else readNotifications
    var refresh by remember { mutableIntStateOf(0) }
    var result by remember(controller, showMessages, refresh) { mutableStateOf<AdminLogResult?>(null) }
    LaunchedEffect(controller, read, refresh) {
        result = null
        result = controller.load(read)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Logs", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showMessages = false }, enabled = showMessages) { Text("Notifications") }
            TextButton(onClick = { showMessages = true }, enabled = !showMessages) { Text("Reservation messages") }
        }
        when (val state = result) {
            null -> CircularProgressIndicator()
            AdminLogResult.Denied -> Text("Verified administrator access is required to view logs.")
            AdminLogResult.Failed -> {
                Text("Could not load logs.")
                TextButton(onClick = { refresh++ }) { Text("Retry") }
            }
            is AdminLogResult.Loaded -> {
                TextButton(onClick = { refresh++ }) { Text("Refresh logs") }
                if (state.rows.isEmpty()) {
                    Text(if (showMessages) "No reservation messages have been recorded." else "No notifications have been recorded.")
                } else {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.rows, key = { it.id }) { row -> AdminLogCard(row, showMessages) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLogCard(row: AdminLogEntry, reservationMessage: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.title, style = MaterialTheme.typography.titleMedium)
            Text(row.body)
            Text("${if (reservationMessage) "Owner" else "From"}: ${row.sender.ifBlank { "Unknown" }}")
            Text("${if (reservationMessage) "Entrant" else "To"}: ${row.recipient.ifBlank { "Unknown" }}")
            if (row.context.isNotBlank()) Text(row.context)
            row.sentAt?.let { Text(DateFormat.getDateTimeInstance().format(Date(it))) }
        }
    }
}
