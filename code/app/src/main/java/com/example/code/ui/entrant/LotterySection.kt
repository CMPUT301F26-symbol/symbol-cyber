/*
 * LotterySection.kt
 * Lottery part of the entrant's campsite details screen (mockup E05):
 * shows the campsite's lottery and its periods (US 01.01.06) and lets the
 * entrant join or leave its waiting list (US 01.02.01, 01.02.02).
 * Known issues: none.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.controller.LotteryController
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.example.code.ui.common.formatDateRange
import com.example.code.ui.common.formatDateTime

/**
 * Shows [lottery] and the entrant's place on its waiting list.
 *
 * @param lottery the campsite's lottery, or null if it has none.
 * @param myEntry this entrant's waiting list entry, or null if not joined.
 * @param now current time in milliseconds.
 * @param busy true while a join or leave request is running.
 * @param statusMessage result of the last action, or null.
 * @param onJoin called when the entrant taps "Join Waiting List".
 * @param onLeave called when the entrant taps "Leave Waiting List".
 */
@Composable
fun LotterySection(
    lottery: Lottery?,
    myEntry: WaitingListEntry?,
    now: Long,
    busy: Boolean,
    statusMessage: String?,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Lottery", style = MaterialTheme.typography.titleMedium)

            if (lottery == null) {
                Text("This campsite has no lottery yet.")
                return@Column
            }

            Text(
                "Registration: ${formatDateTime(lottery.registrationStart)} – " +
                        formatDateTime(lottery.registrationEnd),
            )
            Text("Stay: ${formatDateRange(lottery.bookingStart, lottery.bookingEnd)}")
            Text("Winners drawn: ${lottery.winnerCount}")
            Text(waitingListText(lottery))

            when {
                lottery.status == Lottery.STATUS_DRAWN -> Text("Winners have been drawn.")

                now < lottery.registrationStart ->
                    Text("Registration opens ${formatDateTime(lottery.registrationStart)}.")

                now >= lottery.registrationEnd ->
                    Text("Registration is closed. Winners are being drawn.")

                myEntry != null -> {
                    Text("You are on the waiting list.")
                    OutlinedButton(onClick = onLeave, enabled = !busy) {
                        Text("Leave Waiting List")
                    }
                }

                !LotteryController.hasRoom(lottery) -> Text("The waiting list is full.")

                else -> Button(onClick = onJoin, enabled = !busy) {
                    Text("Join Waiting List")
                }
            }

            if (statusMessage != null) {
                Text(statusMessage, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** Text showing how many entrants have joined, and the cap if there is one. */
private fun waitingListText(lottery: Lottery): String =
    if (lottery.waitlistCapacity == null) {
        "Waiting list: ${lottery.waitingListCount} joined"
    } else {
        "Waiting list: ${lottery.waitingListCount} of ${lottery.waitlistCapacity} spots taken"
    }
