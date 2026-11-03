package com.example.code.ui.owner

import androidx.compose.runtime.Composable
import com.example.code.model.Campsite
import com.example.code.model.ownedCampsites
import com.example.code.ui.entrant.CampsiteDetailsDestination

/** Rechecks ownership on every snapshot, including edits to an open campsite. */
@Composable
fun OwnerCampsiteDetailsDestination(
    campsiteId: String?,
    ownerId: String,
    campsites: List<Campsite>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    CampsiteDetailsDestination(
        campsiteId = campsiteId,
        campsites = ownedCampsites(campsites, ownerId),
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRetry = onRetry,
        onBack = onBack,
    )
}
