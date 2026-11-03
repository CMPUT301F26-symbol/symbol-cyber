/*
 * ReservationRoutes.kt
 * Connects the reservation screens to ReservationController.
 * Known issues: "now" is read when a screen is shown.
 */

package com.example.code.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.code.controller.ReservationController
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry
import com.example.code.ui.entrant.EntrantReservationsScreen
import com.example.code.ui.entrant.WinnerReservationCard
import com.example.code.ui.owner.OwnerReservationsScreen
import kotlinx.coroutines.launch

/**
 * Accept button or reservation status shown to a winner on the campsite
 * details lottery card.
 *
 * @param lottery the drawn lottery.
 * @param myEntry this entrant's waiting list entry.
 * @param reservations all reservations.
 * @param controller reservation controller.
 */
@Composable
fun WinnerReservationContent(
    lottery: Lottery,
    myEntry: WaitingListEntry?,
    reservations: List<Reservation>,
    controller: ReservationController,
) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val reservation = myEntry?.let {
        ReservationController.forLottery(reservations, lottery.id, it.entrantId)
    }

    WinnerReservationCard(
        myEntry = myEntry,
        reservation = reservation,
        busy = busy,
        statusMessage = message,
        onAccept = {
            val entry = myEntry ?: return@WinnerReservationCard
            busy = true
            scope.launch {
                message = try {
                    if (controller.acceptWin(lottery, entry) != null) {
                        "Reservation confirmed."
                    } else {
                        "You already accepted this reservation."
                    }
                } catch (e: Exception) {
                    "Could not reserve: ${e.message}"
                }
                busy = false
            }
        },
    )
}

/**
 * Entrant's Reservations tab.
 *
 * @param reservations all reservations.
 * @param deviceId this entrant's ID.
 */
@Composable
fun EntrantReservationsRoute(reservations: List<Reservation>, deviceId: String) {
    EntrantReservationsScreen(
        reservations = ReservationController.activeForEntrant(
            reservations,
            deviceId,
            System.currentTimeMillis(),
        ),
    )
}

/**
 * Owner's Reservations tab.
 *
 * @param reservations all reservations.
 * @param deviceId this owner's ID.
 */
@Composable
fun OwnerReservationsRoute(reservations: List<Reservation>, deviceId: String) {
    val now = System.currentTimeMillis()
    OwnerReservationsScreen(
        bookedDates = ReservationController.bookedDates(reservations, deviceId, now),
        reservations = ReservationController.activeForOwner(reservations, deviceId, now),
    )
}
