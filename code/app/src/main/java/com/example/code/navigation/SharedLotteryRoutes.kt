// Connects the shared lotteries screen to Firestore, saving each change and showing how it went.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.code.controller.SharedLotteryController
import com.example.code.data.FirestoreSharedLotteryRepository
import com.example.code.data.SharedLotteryRepository
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.ui.owner.SharedLotteriesScreen
import kotlinx.coroutines.launch

/**
 * Owner's shared lotteries screen, saving added or removed campsites to Firestore.
 *
 * @param lotteries every lottery in the app.
 * @param campsites every campsite in the app.
 * @param deviceId this owner's device id.
 * @param onBack called when the owner goes back.
 * @param repository where changes are saved.
 * @param clock gives the current time in milliseconds.
 */
@Composable
fun SharedLotteriesRoute(
    lotteries: List<Lottery>,
    campsites: List<Campsite>,
    deviceId: String,
    onBack: () -> Unit,
    repository: SharedLotteryRepository = remember { FirestoreSharedLotteryRepository() },
    clock: () -> Long = System::currentTimeMillis,
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    SharedLotteriesScreen(
        lotteries = lotteries,
        campsites = campsites,
        ownerId = deviceId,
        now = clock(),
        busy = busy,
        message = message,
        onAddCampsite = { lottery, campsite ->
            busy = true
            scope.launch {
                message = try {
                    // Check here first so the owner gets a clear reason if it isn't allowed
                    SharedLotteryController.addCampsite(lottery, campsite, deviceId, clock())
                    repository.addCampsite(lottery.id, campsite, deviceId)
                    "${campsite.name} was added to the lottery."
                } catch (e: IllegalArgumentException) {
                    e.message
                } catch (e: Exception) {
                    "Could not add ${campsite.name}. Try again."
                }
                busy = false
            }
        },
        onRemoveCampsite = { lottery, campsite ->
            busy = true
            scope.launch {
                message = try {
                    SharedLotteryController.removeCampsite(lottery, campsite, deviceId, clock())
                    repository.removeCampsite(lottery.id, campsite, deviceId)
                    "${campsite.name} was removed from the lottery."
                } catch (e: IllegalArgumentException) {
                    e.message
                } catch (e: Exception) {
                    "Could not remove ${campsite.name}. Try again."
                }
                busy = false
            }
        },
        onBack = onBack,
    )
}
