package com.example.code.ui.owner

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.ownedCampsites
import com.example.code.ui.entrant.CampsiteDetailsDestination

/**
 * Rechecks ownership on every snapshot and opens the lottery creation form.
 *
 * [onPublishLottery] receives the actual owned campsite and the form settings.
 * Omit it in read-only previews; application navigation supplies persistence.
 */
@Composable
fun OwnerCampsiteDetailsDestination(
    campsiteId: String?,
    ownerId: String,
    campsites: List<Campsite>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    /** AI USAGE
     * //The method, onpublishLottery, and lotteryContent was made by GPT6-Sol
     * " "Based on the required project specifications provided and the existing code, I need help adding in code for US 2.04.01, 03,04, 05, and 07.
     * Ensure that the new added features for the lottery do not interfere with the reservation, waitingList, etc.
     * Also, understand from the specifications that many other classes will also depend on new features of the Lottery.
     * Currently, there is a model class, controller, and a LOtteryRepository", 11-05-2026
     * */
    onPublishLottery: (suspend (Campsite, Lottery) -> String)? = null,
) {
    val owned = ownedCampsites(campsites, ownerId)
    val campsite = owned.firstOrNull { it.id == campsiteId }
    var creating by rememberSaveable(campsiteId, ownerId) { mutableStateOf(false) }
    var published by rememberSaveable(campsiteId, ownerId) { mutableStateOf(false) }
    if (creating && campsite != null && !isLoading && errorMessage == null && onPublishLottery != null) {
        CreateLotteryScreen(
            campsite = campsite,
            onPublish = { settings -> onPublishLottery(campsite, settings) },
            onPublished = {
                creating = false
                published = true
            },
            onCancel = { creating = false },
        )
        return
    }
    CampsiteDetailsDestination(
        campsiteId = campsiteId,
        campsites = owned,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRetry = onRetry,
        onBack = onBack,
        lotteryContent = {
            if (published) Text("Lottery published. View it in the Lotteries tab.")
            if (onPublishLottery != null && campsite != null) {
                Button(onClick = { published = false; creating = true }) {
                    Text("Create lottery")
                }
            }
        },
    )
}