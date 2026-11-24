// Owner screen that lists lotteries open for sharing and lets an owner add or remove only their own campsites. Known issue is that only the owner who created a lottery sees its waiting list.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.code.controller.LotteryController
import com.example.code.controller.SharedLotteryController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.ui.common.formatDateRange
import com.example.code.ui.common.formatDateTime

/**
 * Lists lotteries the owner can share campsites in, and lets them add or remove only their own campsites.
 *
 * @param lotteries every lottery in the app.
 * @param campsites every campsite in the app, used to tell whose campsite is whose.
 * @param ownerId the device id of the owner using the screen.
 * @param now the current time in milliseconds.
 * @param busy true while a change is being saved, so buttons can't be tapped twice.
 * @param message the result of the last change, or null if there is none.
 * @param onAddCampsite called with the lottery and the owner's campsite to add to it.
 * @param onRemoveCampsite called with the lottery and the owner's campsite to remove from it.
 * @param onBack called when the owner taps Back.
 */
@Composable
fun SharedLotteriesScreen(
    lotteries: List<Lottery>,
    campsites: List<Campsite>,
    ownerId: String,
    now: Long = System.currentTimeMillis(),
    busy: Boolean = false,
    message: String? = null,
    onAddCampsite: (Lottery, Campsite) -> Unit = { _, _ -> },
    onRemoveCampsite: (Lottery, Campsite) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
) {
    val shown = SharedLotteryController.sharedLotteriesFor(lotteries, ownerId, campsites, now)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.testTag("backButton"), // for testing
        ) {
            Text("Back")
        }

        Text("Shared lotteries", style = MaterialTheme.typography.headlineSmall)
        Text("Add your campsites to a lottery for the same stay. You can only change your own campsites.")

        if (message != null) {
            Text(message)
        }

        if (shown.isEmpty()) {
            Text("No lotteries are open for sharing right now.")
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = shown, key = { it.id }) { lottery ->
                SharedLotteryCard(
                    lottery = lottery,
                    campsites = campsites,
                    ownerId = ownerId,
                    canChange = SharedLotteryController.canChangeCampsites(lottery, now) && !busy,
                    onAddCampsite = { onAddCampsite(lottery, it) },
                    onRemoveCampsite = { onRemoveCampsite(lottery, it) },
                )
            }
        }
    }
}

/**
 * Displays one shared lottery with its campsites, with Remove buttons only on the owner's campsites.
 *
 * @param lottery the lottery to show.
 * @param campsites every campsite in the app.
 * @param ownerId the device id of the owner using the screen.
 * @param canChange true if campsites can still be added or removed.
 * @param onAddCampsite called with the owner's campsite to add.
 * @param onRemoveCampsite called with the owner's campsite to remove.
 */
@Composable
private fun SharedLotteryCard(
    lottery: Lottery,
    campsites: List<Campsite>,
    ownerId: String,
    canChange: Boolean,
    onAddCampsite: (Campsite) -> Unit,
    onRemoveCampsite: (Campsite) -> Unit,
) {
    val included = LotteryController.includedCampsites(lottery)

    // The owner's campsites that aren't in this lottery yet
    val canAdd = campsites.filter { it.ownerId == ownerId && !LotteryController.includesCampsite(lottery, it.id) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sharedLottery-${lottery.id}"), // for testing
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Stay: ${formatDateRange(lottery.bookingStart, lottery.bookingEnd)}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text("Registration closes ${formatDateTime(lottery.registrationEnd)}")
            Text(if (lottery.ownerId == ownerId) "Created by you" else "Created by another owner")

            Text("Campsites in this lottery", style = MaterialTheme.typography.titleSmall)
            included.forEach { entry ->
                val mine = campsites.firstOrNull { it.id == entry.id && it.ownerId == ownerId }

                if (mine == null) {
                    // Someone else's campsite, so it can only be looked at
                    Text("${entry.name} (another owner's)")
                } else {
                    Text("${entry.name} (yours)")
                    if (canChange) {
                        OutlinedButton(
                            onClick = { onRemoveCampsite(mine) },
                            modifier = Modifier.testTag("removeCampsite-${lottery.id}-${mine.id}"), // for testing
                        ) {
                            Text("Remove ${mine.name}")
                        }
                    }
                }
            }

            if (!canChange) {
                Text("Campsites can't change once entrants join or registration closes.")
            } else {
                canAdd.forEach { campsite ->
                    Button(
                        onClick = { onAddCampsite(campsite) },
                        modifier = Modifier.testTag("addCampsite-${lottery.id}-${campsite.id}"), // for testing
                    ) {
                        Text("Add ${campsite.name}")
                    }
                }
            }
        }
    }
}
