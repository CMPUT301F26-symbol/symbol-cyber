/*
 * OwnerReservationsScreen.kt
 * Owner's Reservations tab (mockup O06): active reservations for the
 * owner's campsites (US 02.07.01).
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Reservation
import com.example.code.ui.common.formatDateRange

/**
 * Shows active reservations for the owner's campsites.
 *
 * @param reservations active reservations, soonest first.
 */
@Composable
fun OwnerReservationsScreen(
    reservations: List<Reservation>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Text("Reservations", style = MaterialTheme.typography.headlineSmall) }

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
                }
            }
        }
    }
}
