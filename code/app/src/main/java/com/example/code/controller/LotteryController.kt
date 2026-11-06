/*
 * LotteryController.kt
 * Validates and publishes owner lottery settings, checks registration and
 * waiting-list capacity, and supplies entrant display names. Models hold data;
 * DrawController handles drawing and repositories handle persistence.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.data.LotteryCreationRepository
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry

/** Rules for lotteries. */
object LotteryController {
    //The two below methods, publishLottery and validateCreation by GPT6-Sol "Based on the required project specifications provided and the existing code, I need help adding in code for US 2.04.01, 03,04, 05, and 07", 11-05-2026
    /**
     * Publishes settings for an owned campsite (US 02.04.01, .03, .04, .05, .07).
     * Identity comes from [campsite], never editable form fields. Draw results
     * remain the responsibility of DrawController and waiting-list entries.
     * Returns the saved Firestore document ID; failures propagate to the form.
     */
    suspend fun publishLottery(
        repository: LotteryCreationRepository,
        campsite: Campsite,
        ownerId: String,
        settings: Lottery,
    ): String {
        require(ownerId.isNotBlank() && campsite.ownerId == ownerId) {
            "You can only create lotteries for your own campsites."
        }
        val lottery = settings.copy(
            id = "",
            campsiteId = campsite.id,
            campsiteName = campsite.name,
            ownerId = campsite.ownerId,
            waitingListCount = 0,
            status = Lottery.STATUS_OPEN,
        )
        validateCreation(lottery)
        return repository.createLottery(lottery)
    }

    /** Shared form/persistence validation. Throws with a message for the owner. */
    fun validateCreation(lottery: Lottery) {
        require(lottery.campsiteId.isNotBlank() && '/' !in lottery.campsiteId) {
            "A saved campsite is required."
        }
        require(lottery.campsiteName.isNotBlank()) { "The campsite needs a name." }
        require(lottery.ownerId.isNotBlank()) { "An owner ID is required." }
        require(lottery.registrationStart < lottery.registrationEnd) {
            "Registration must start before it ends."
        }
        require(lottery.registrationEnd <= lottery.bookingStart) {
            "Registration must end on or before the booking period starts."
        }
        require(lottery.bookingStart < lottery.bookingEnd) {
            "The booking period must start before it ends."
        }
        require(lottery.winnerCount >= 1) { "Choose at least one winner." }
        require(lottery.waitlistCapacity == null || lottery.waitlistCapacity >= lottery.winnerCount) {
            "Waiting-list capacity must be at least the number of winners, or left blank."
        }
    }
    /**
     * Returns true if entrants can join or leave the waiting list of
     * [lottery] at [now]: it is not drawn and [now] is inside the
     * registration period.
     */
    fun isRegistrationOpen(lottery: Lottery, now: Long): Boolean =
        lottery.status == Lottery.STATUS_OPEN &&
                now in lottery.registrationStart until lottery.registrationEnd

    /** Returns true if the waiting list of [lottery] has room for one more entrant. */
    fun hasRoom(lottery: Lottery): Boolean =
        lottery.waitlistCapacity == null || lottery.waitingListCount < lottery.waitlistCapacity

    /**
     * Returns the name shown for [entry], falling back to a short device ID
     * when the entrant has no name.
     */
    fun entrantDisplayName(entry: WaitingListEntry): String =
        entry.entrantName.ifBlank { "Entrant ${entry.entrantId.take(6)}" }
}
