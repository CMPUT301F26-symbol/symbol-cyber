/*
 * LotteryRoutes.kt
 * Connects the waiting list screens to WaitingListController: collects the
 * live data, runs actions in a coroutine and shows their result.
 * Known issues: "now" is read when the screen is shown; the screen does not
 * refresh by itself when registration opens or closes.
 */

package com.example.code.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.code.controller.WaitingListController
import com.example.code.data.JoinResult
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.example.code.ui.entrant.LotterySection
import com.example.code.ui.owner.OwnerLotteriesScreen
import com.example.code.ui.owner.OwnerWaitingListScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Lottery card on the entrant's campsite details screen.
 *
 * @param lottery the campsite's lottery, or null.
 * @param deviceId this entrant's ID.
 * @param controller waiting list controller.
 * @param drawnContent extra content shown after the draw; receives this
 * entrant's waiting list entry.
 */
@Composable
fun EntrantLotteryCard(
    lottery: Lottery?,
    deviceId: String,
    controller: WaitingListController,
    drawnContent: @Composable (WaitingListEntry?) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val entries by remember(lottery?.id) {
        lottery?.let { controller.waitingList(it.id) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    // Re-read the clock every second so the Join button appears as soon as
    // registration opens, without leaving the screen.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    LotterySection(
        lottery = lottery,
        myEntry = entries.firstOrNull { it.entrantId == deviceId },
        now = now,
        busy = busy,
        statusMessage = message,
        onJoin = {
            val target = lottery ?: return@LotterySection
            busy = true
            scope.launch {
                message = try {
                    when (controller.join(target, deviceId, "")) {
                        JoinResult.JOINED -> "You joined the waiting list."
                        JoinResult.ALREADY_JOINED -> "You are already on the waiting list."
                        JoinResult.FULL -> "Sorry, the waiting list is full."
                        JoinResult.CLOSED -> "Registration is not open."
                    }
                } catch (e: Exception) {
                    "Could not join: ${e.message}"
                }
                busy = false
            }
        },
        onLeave = {
            val target = lottery ?: return@LotterySection
            busy = true
            scope.launch {
                message = try {
                    if (controller.leave(target, deviceId)) {
                        "You left the waiting list."
                    } else {
                        "You are not on the waiting list."
                    }
                } catch (e: Exception) {
                    "Could not leave: ${e.message}"
                }
                busy = false
            }
        },
        drawnContent = drawnContent,
    )
}

/**
 * Owner's Lotteries tab.
 *
 * @param lotteries all lotteries.
 * @param deviceId this owner's ID.
 * @param onOpenWaitingList called with the lottery to open.
 */
@Composable
fun OwnerLotteriesRoute(
    lotteries: List<Lottery>,
    deviceId: String,
    onOpenWaitingList: (Lottery) -> Unit,
) {
    OwnerLotteriesScreen(
        lotteries = WaitingListController.lotteriesOfOwner(lotteries, deviceId),
        onOpenWaitingList = onOpenWaitingList,
    )
}

/**
 * Owner's waiting list screen for one lottery.
 *
 * @param lottery the lottery.
 * @param deviceId this owner's ID, used as the sender.
 * @param controller waiting list controller.
 * @param onBack called when the owner goes back.
 */
@Composable
fun OwnerWaitingListRoute(
    lottery: Lottery,
    deviceId: String,
    controller: WaitingListController,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val entries by remember(lottery.id) { controller.waitingList(lottery.id) }
        .collectAsState(initial = emptyList())
    var message by remember { mutableStateOf<String?>(null) }

    OwnerWaitingListScreen(
        lottery = lottery,
        entries = entries,
        statusMessage = message,
        onNotify = { text ->
            scope.launch {
                message = try {
                    val sent = controller.notifyWaitingList(lottery, deviceId, entries, text)
                    "Notification sent to $sent entrants."
                } catch (e: Exception) {
                    "Could not send: ${e.message}"
                }
            }
        },
        onBack = onBack,
    )
}
