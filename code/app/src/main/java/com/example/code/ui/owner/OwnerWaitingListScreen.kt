/*
 * OwnerWaitingListScreen.kt
 * Owner's view of one lottery's waiting list (mockup O05): the entrants
 * who joined (US 02.09.01).
 * Known issues: notifying the waiting list is added with US 02.10.01.
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.controller.LotteryController
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.example.code.ui.common.formatDateTime

/**
 * Shows the entrants on [lottery]'s waiting list.
 *
 * @param lottery the lottery.
 * @param entries entrants on its waiting list.
 * @param onBack called when the owner taps "Back".
 */
@Composable
fun OwnerWaitingListScreen(
    lottery: Lottery,
    entries: List<WaitingListEntry>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Waiting list: ${lottery.campsiteName}", style = MaterialTheme.typography.headlineSmall)
        Text("${entries.size} entrants joined")

        if (entries.isEmpty()) {
            Text("No entrants have joined yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entries, key = { it.entrantId }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(LotteryController.entrantDisplayName(entry), style = MaterialTheme.typography.titleSmall)
                            Text("Joined ${formatDateTime(entry.joinedAt)}")
                        }
                    }
                }
            }
        }
    }
}
