/*
 * EntrantReservationsScreen.kt
 * Entrant's Reservations tab (mockup E09): the entrant's active
 * reservations (US 01.06.01) and past reservations (US 01.06.02).
 * Known issues: none.
 */

package com.example.code.ui.entrant

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Reservation
import com.example.code.ui.common.formatDateRange

/**
 * Lists the entrant's active and past reservations; an active one can be
 * cancelled (US 01.07.01).
 *
 * @param reservations active reservations, soonest first.
 * @param pastReservations reservations whose stay has ended, most recent first.
 * @param onCancel cancels a reservation after the entrant confirms (US 01.07.01); null hides the button.
 * @param statusMessage result of the last action, or null.
 */
@Composable
fun EntrantReservationsScreen(
    reservations: List<Reservation>,
    modifier: Modifier = Modifier,
    pastReservations: List<Reservation> = emptyList(),
    onCancel: ((Reservation) -> Unit)? = null,
    statusMessage: String? = null,
) {
    var cancelling by remember { mutableStateOf<Reservation?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("My reservations", style = MaterialTheme.typography.headlineSmall)
        if (statusMessage != null) {
            Text(statusMessage, style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (reservations.isEmpty()) {
                item { Text("You have no active reservations.") }
            }
            items(reservations, key = { it.id }) { reservation ->
                ReservationCard(
                    reservation,
                    status = "Active",
                    onCancel = onCancel?.let { { cancelling = reservation } },
                )
            }

            item {
                Text(
                    "Past reservations",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (pastReservations.isEmpty()) {
                item { Text("You have no past reservations.") }
            }
            items(pastReservations, key = { "past-${it.id}" }) { reservation ->
                ReservationCard(reservation, status = "Completed")
            }
        }
    }

    cancelling?.let { reservation ->
        AlertDialog(
            onDismissRequest = { cancelling = null },
            title = { Text("Cancel this reservation?") },
            text = {
                Text("${reservation.campsiteName}, ${formatDateRange(reservation.startDate, reservation.endDate)}. This can't be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    onCancel?.invoke(reservation)
                    cancelling = null
                }) { Text("Cancel reservation", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { cancelling = null }) { Text("Keep reservation") } },
        )
    }
}

/** One reservation: campsite, stay and [status]. */
@Composable
private fun ReservationCard(reservation: Reservation, status: String, onCancel: (() -> Unit)? = null) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(reservation.campsiteName, style = MaterialTheme.typography.titleMedium)
            Text("Stay: ${formatDateRange(reservation.startDate, reservation.endDate)}")
            Text("Status: $status")
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text("Cancel reservation", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
