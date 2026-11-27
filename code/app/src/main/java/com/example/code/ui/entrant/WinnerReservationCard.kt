/*
 * WinnerReservationCard.kt
 * Shown on the campsite details lottery card after the draw: a winner
 * picks available dates (US 01.04.01), reads the campsite rules and policies and confirms them (US 02.11.01),
 * accepts their reservation or declines it (US 01.04.03), then sees it
 * confirmed, rejected or declined.
 * Known issues: none.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.code.controller.STATUS_DECLINED
import com.example.code.controller.endDayOptions
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import com.example.code.ui.common.formatDate
import com.example.code.ui.common.formatDateRange

/**
 * Accept button or reservation status for a lottery winner.
 *
 * @param myEntry this entrant's waiting list entry, or null if not joined.
 * @param reservation the entrant's reservation from this lottery, or null.
 * @param busy true while the reservation is being saved.
 * @param statusMessage result of the last action, or null.
 * @param onAccept called when the winner taps "Accept reservation", with
 * true if they confirmed the rules and policies.
 * @param rules campsite rules the winner must read; blank if none.
 * @param cancellationPolicy campsite cancellation policy; blank if none.
 * @param availableDays days the winner can still book (US 01.04.01); used
 * only with [onAcceptStay].
 * @param onAcceptStay if set, the winner picks a first and last day and this
 * is called instead of [onAccept], with the rules answer and the two days.
 * @param onDecline if set, shows a Decline button; called after the winner confirms (US 01.04.03).
 */
@Composable
fun WinnerReservationCard(
    myEntry: WaitingListEntry?,
    reservation: Reservation?,
    busy: Boolean,
    statusMessage: String?,
    onAccept: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    rules: String = "",
    cancellationPolicy: String = "",
    availableDays: List<Long> = emptyList(),
    onAcceptStay: ((Boolean, Long, Long) -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
) {
    if (myEntry?.status != WaitingListEntry.STATUS_WON) return
    val mustConfirm = rules.isNotBlank() || cancellationPolicy.isNotBlank()
    var confirmed by remember { mutableStateOf(false) }
    var arrive by remember { mutableStateOf<Long?>(null) }
    var leave by remember { mutableStateOf<Long?>(null) }
    val datesChosen = onAcceptStay == null || (arrive != null && leave != null)
    var confirmingDecline by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            reservation == null -> {
                if (onAcceptStay != null) {
                    StayDatePicker(
                        availableDays = availableDays,
                        arrive = arrive,
                        leave = leave,
                        onArrive = {
                            arrive = it
                            leave = null
                        },
                        onLeave = { leave = it },
                    )
                }
                if (mustConfirm) {
                    RulesCard(rules = rules, cancellationPolicy = cancellationPolicy)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(value = confirmed, role = Role.Checkbox, onValueChange = { confirmed = it })
                            .testTag("confirmRules"),
                    ) {
                        Checkbox(checked = confirmed, onCheckedChange = null)
                        Text("I have read and agree to the campsite rules and policies")
                    }
                }
                Button(
                    onClick = {
                        val first = arrive
                        val last = leave
                        if (onAcceptStay != null && first != null && last != null) {
                            onAcceptStay(confirmed, first, last)
                        } else {
                            onAccept(confirmed)
                        }
                    },
                    enabled = !busy && (!mustConfirm || confirmed) && datesChosen,
                ) {
                    Text("Accept reservation")
                }
                if (onDecline != null) {
                    OutlinedButton(onClick = { confirmingDecline = true }, enabled = !busy) {
                        Text("Decline")
                    }
                }
            }

            reservation.status == STATUS_DECLINED ->
                Text("You declined this reservation.")

            reservation.status == Reservation.STATUS_REJECTED ->
                Text("The owner rejected this reservation: ${reservation.rejectionReason}")
            //Following block(s) of code were generated by GPT-6 Sol, "Please look at my code base and implement User Story 2.04.02", November 17, 2026

            // === US 02.04.02: BEGIN identify the accepted campsite ===
            else -> {
                Text("Campsite: ${reservation.campsiteName}")
                Text("Reserved: ${formatDateRange(reservation.startDate, reservation.endDate)}")
            }
            // === US 02.04.02: END identify the accepted campsite ===
        }

        if (statusMessage != null) {
            Text(statusMessage, style = MaterialTheme.typography.bodySmall)
        }
    }

    if (confirmingDecline && onDecline != null) {
        AlertDialog(
            onDismissRequest = { confirmingDecline = false },
            title = { Text("Decline this reservation?") },
            text = { Text("Your spot goes back to the campsite owner. You can't accept it later.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmingDecline = false
                    onDecline()
                }) { Text("Decline reservation") }
            },
            dismissButton = { TextButton(onClick = { confirmingDecline = false }) { Text("Keep it") } },
        )
    }
}

/** Lets the winner pick the first and last day of the stay from [availableDays] (US 01.04.01). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StayDatePicker(
    availableDays: List<Long>,
    arrive: Long?,
    leave: Long?,
    onArrive: (Long) -> Unit,
    onLeave: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Choose your dates", style = MaterialTheme.typography.titleSmall)
        if (availableDays.isEmpty()) {
            Text("All dates in this booking period are already booked.")
            return@Column
        }
        Text("First day", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            availableDays.forEach { day ->
                FilterChip(
                    selected = day == arrive,
                    onClick = { onArrive(day) },
                    label = { Text(formatDate(day)) },
                    modifier = Modifier.testTag("arrive-$day"),
                )
            }
        }
        if (arrive != null) {
            Text("Last day", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                endDayOptions(availableDays, arrive).forEach { day ->
                    FilterChip(
                        selected = day == leave,
                        onClick = { onLeave(day) },
                        label = { Text(formatDate(day)) },
                        modifier = Modifier.testTag("leave-$day"),
                    )
                }
            }
        }
        if (arrive != null && leave != null) {
            Text("Your stay: ${formatDateRange(arrive, leave)}")
        }
    }
}

/** The campsite rules and cancellation policy the winner must read. */
@Composable
private fun RulesCard(rules: String, cancellationPolicy: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Before you reserve", style = MaterialTheme.typography.titleSmall)
            if (rules.isNotBlank()) {
                Text("Campsite rules", style = MaterialTheme.typography.labelLarge)
                Text(rules)
            }
            if (cancellationPolicy.isNotBlank()) {
                Text("Cancellation policy", style = MaterialTheme.typography.labelLarge)
                Text(cancellationPolicy)
            }
        }
    }
}
