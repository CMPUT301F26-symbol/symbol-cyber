/*
 * EntrantReservationsScreen.kt
 * Entrant's Reservations tab (mockup E09): the entrant's active
 * reservations (US 01.06.01).
 * Known issues: past reservations (US 01.06.02) are not shown yet.
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
 * Lists the entrant's active reservations.
 *
 * @param reservations active reservations, soonest first.
 */
@Composable
fun EntrantReservationsScreen(
    reservations: List<Reservation>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("My reservations", style = MaterialTheme.typography.headlineSmall)

        if (reservations.isEmpty()) {
            Text("You have no active reservations.")
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reservations, key = { it.id }) { reservation ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(reservation.campsiteName, style = MaterialTheme.typography.titleMedium)
                        Text("Stay: ${formatDateRange(reservation.startDate, reservation.endDate)}")
                        Text("Status: Active")
                    }
                }
            }
        }
    }
}
