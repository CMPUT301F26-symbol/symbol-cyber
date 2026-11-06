/*
 * OwnerLotteriesScreen.kt
 * Owner's Lotteries tab: lists the owner's lotteries and opens each
 * lottery's waiting list (US 02.09.01).
 * Known issues: the "Create lottery" form (US 02.04.01) is a separate task.
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Lottery
import com.example.code.ui.common.formatDateTime

/**
 * Lists the owner's lotteries.
 *
 * @param lotteries lotteries created by this owner.
 * @param onOpenWaitingList called with the lottery whose waiting list to show.
 */
@Composable
fun OwnerLotteriesScreen(
    lotteries: List<Lottery>,
    onOpenWaitingList: (Lottery) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("My lotteries", style = MaterialTheme.typography.headlineSmall)

        if (lotteries.isEmpty()) {
            Text("You have no lotteries yet.")
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lotteries, key = { it.id }) { lottery ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(lottery.campsiteName, style = MaterialTheme.typography.titleMedium)
                        Text("Registration closes ${formatDateTime(lottery.registrationEnd)}")
                        Text(if (lottery.status == Lottery.STATUS_DRAWN) "Winners drawn" else "Open")
                        OutlinedButton(onClick = { onOpenWaitingList(lottery) }) {
                            Text("Waiting list (${lottery.waitingListCount})")
                        }
                    }
                }
            }
        }
    }
}
