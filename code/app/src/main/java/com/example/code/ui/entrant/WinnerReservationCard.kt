/*
 * WinnerReservationCard.kt
 * Shown on the campsite details lottery card after the draw: a winner
 * accepts their reservation, then sees it confirmed or rejected.
 * Known issues: declining a win (US 01.04.03) is not available yet.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import com.example.code.ui.common.formatDateRange

/**
 * Accept button or reservation status for a lottery winner.
 *
 * @param myEntry this entrant's waiting list entry, or null if not joined.
 * @param reservation the entrant's reservation from this lottery, or null.
 * @param busy true while the reservation is being saved.
 * @param statusMessage result of the last action, or null.
 * @param onAccept called when the winner taps "Accept reservation".
 */
@Composable
fun WinnerReservationCard(
    myEntry: WaitingListEntry?,
    reservation: Reservation?,
    busy: Boolean,
    statusMessage: String?,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (myEntry?.status != WaitingListEntry.STATUS_WON) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            reservation == null -> Button(onClick = onAccept, enabled = !busy) {
                Text("Accept reservation")
            }

            reservation.status == Reservation.STATUS_REJECTED ->
                Text("The owner rejected this reservation: ${reservation.rejectionReason}")

            else ->
                Text("Reserved: ${formatDateRange(reservation.startDate, reservation.endDate)}")
        }

        if (statusMessage != null) {
            Text(statusMessage, style = MaterialTheme.typography.bodySmall)
        }
    }
}
