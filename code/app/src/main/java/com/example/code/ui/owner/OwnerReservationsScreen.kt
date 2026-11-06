/*
 * OwnerReservationsScreen.kt
 * Owner's Reservations tab (mockups O03, O06, O08): booked dates for each
 * campsite (US 02.06.01), active reservations (US 02.07.01) and rejecting a
 * reservation with a reason (US 02.08.01).
 * Known issues: past and cancelled reservations (US 02.07.02, 02.07.03) are
 * not shown yet.
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.code.controller.BookedDates
import com.example.code.model.Reservation
import com.example.code.ui.common.formatDateRange

/**
 * Shows booked dates and active reservations for the owner's campsites.
 *
 * @param bookedDates booked stays grouped by campsite.
 * @param reservations active reservations, soonest first.
 * @param statusMessage result of the last action, or null.
 * @param onReject called with a reservation and the owner's reason.
 */
@Composable
fun OwnerReservationsScreen(
    bookedDates: List<BookedDates>,
    reservations: List<Reservation>,
    statusMessage: String?,
    onReject: (Reservation, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rejecting by remember { mutableStateOf<Reservation?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text("Reservations", style = MaterialTheme.typography.headlineSmall) }

        if (statusMessage != null) {
            item { Text(statusMessage, style = MaterialTheme.typography.bodySmall) }
        }

        item { Text("Booked dates", style = MaterialTheme.typography.titleMedium) }
        if (bookedDates.isEmpty()) {
            item { Text("No dates are booked.") }
        }
        items(bookedDates, key = { "booked-${it.campsiteName}" }) { campsite ->
            Column {
                Text(campsite.campsiteName, style = MaterialTheme.typography.titleSmall)
                campsite.stays.forEach { (start, end) -> Text(formatDateRange(start, end)) }
            }
        }

        item { Text("Active reservations", style = MaterialTheme.typography.titleMedium) }
        if (reservations.isEmpty()) {
            item { Text("No active reservations.") }
        }
        items(reservations, key = { it.id }) { reservation ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(reservation.campsiteName, style = MaterialTheme.typography.titleSmall)
                    Text(reservation.entrantName.ifBlank { "Entrant ${reservation.entrantId.take(6)}" })
                    Text(formatDateRange(reservation.startDate, reservation.endDate))
                    OutlinedButton(onClick = { rejecting = reservation }) { Text("Reject") }
                }
            }
        }
    }

    rejecting?.let { reservation ->
        RejectDialog(
            reservation = reservation,
            onConfirm = { reason ->
                onReject(reservation, reason)
                rejecting = null
            },
            onDismiss = { rejecting = null },
        )
    }
}

/** Dialog where the owner enters the reason for rejecting [reservation]. */
@Composable
private fun RejectDialog(
    reservation: Reservation,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject reservation?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${reservation.campsiteName}, " +
                        formatDateRange(reservation.startDate, reservation.endDate),
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason) }, enabled = reason.isNotBlank()) {
                Text("Reject reservation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
