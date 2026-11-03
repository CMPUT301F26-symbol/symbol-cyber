/*
 * LotterySection.kt
 * Lottery part of the entrant's campsite details screen (mockup E05):
 * shows the campsite's lottery, its periods and whether registration is
 * open (US 01.01.06).
 * Known issues: the Join Waiting List button is added with US 01.02.01.
 */

package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.controller.LotteryController
import com.example.code.model.Lottery
import com.example.code.ui.common.formatDateRange
import com.example.code.ui.common.formatDateTime

/**
 * Shows [lottery] on the campsite details screen.
 *
 * @param lottery the campsite's lottery, or null if it has none.
 * @param now current time in milliseconds.
 */
@Composable
fun LotterySection(
    lottery: Lottery?,
    now: Long,
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

                !LotteryController.hasRoom(lottery) -> Text("The waiting list is full.")

                else -> Text("Registration is open.")
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
