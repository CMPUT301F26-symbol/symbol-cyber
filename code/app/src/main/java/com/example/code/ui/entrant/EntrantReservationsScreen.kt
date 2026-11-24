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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Reservation
import com.example.code.ui.common.formatDateRange

/**
 * Lists the entrant's active and past reservations.
 *
 * @param reservations active reservations, soonest first.
 * @param pastReservations reservations whose stay has ended, most recent first.
 */
@Composable
fun EntrantReservationsScreen(
    reservations: List<Reservation>,
    modifier: Modifier = Modifier,
    pastReservations: List<Reservation> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("My reservations", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (reservations.isEmpty()) {
                item { Text("You have no active reservations.") }
            }
            items(reservations, key = { it.id }) { reservation ->
                ReservationCard(reservation, status = "Active")
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
}

/** One reservation: campsite, stay and [status]. */
@Composable
private fun ReservationCard(reservation: Reservation, status: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(reservation.campsiteName, style = MaterialTheme.typography.titleMedium)
            Text("Stay: ${formatDateRange(reservation.startDate, reservation.endDate)}")
            Text("Status: $status")
        }
    }
}
