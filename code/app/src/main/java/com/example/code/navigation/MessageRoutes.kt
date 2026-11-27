/*
 * MessageRoutes.kt
 * Connects the conversation screen to MessageController for a campsite
 * owner and an entrant (US 02.12.01, US 01.14.01).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.code.controller.MessageController
import com.example.code.data.FirestoreMessageRepository
import com.example.code.model.Reservation
import com.example.code.ui.common.ConversationScreen
import kotlinx.coroutines.launch

/**
 * Conversation about reservation [reservationId] for the user [deviceId].
 *
 * @param reservations all reservations.
 * @param userName this user's name, shown with their messages.
 * @param onBack goes back.
 */
@Composable
fun ConversationRoute(
    reservationId: String?,
    reservations: List<Reservation>,
    deviceId: String,
    userName: String,
    onBack: () -> Unit,
    controller: MessageController = remember { MessageController(FirestoreMessageRepository()) },
) {
    val reservation = reservations.firstOrNull { it.id == reservationId }
    if (reservation == null || !MessageController.canMessage(reservation, deviceId)) {
        Text("Conversation not found.")
        return
    }
    val messages by remember(reservation.id) { controller.conversation(reservation) }
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val iAmOwner = deviceId == reservation.ownerId
    val other = if (iAmOwner) {
        reservation.entrantName.ifBlank { "the entrant" }
    } else {
        "the owner"
    }

    ConversationScreen(
        title = "${reservation.campsiteName}: messages with $other",
        messages = messages,
        myId = deviceId,
        canSend = reservation.status == Reservation.STATUS_ACTIVE,
        busy = busy,
        errorMessage = error,
        onSend = { text ->
            busy = true
            error = null
            scope.launch {
                try {
                    controller.send(reservation, deviceId, userName, text)
                } catch (e: Exception) {
                    error = "Could not send: ${e.message}"
                }
                busy = false
            }
        },
        onBack = onBack,
    )
}
