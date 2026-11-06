package com.example.code.data

import com.example.code.controller.LotteryController
import com.example.code.model.Lottery


/** AI USAGE
 * The basis code for the following file, LotteryCreationRepository, was made by GPT6-Sol
 * "Based on the required project specifications provided and the existing code,
 * I need help adding in code for US 2.04.01, 03,04, 05, and 07. Ensure that the new added features for the lottery
 * do not interfere with the reservation, waitingList, etc.
 * Also, understand from the specifications that many other classes will also depend on new features of the Lottery.
 * Currently, there is a model class, controller, and a LOtteryRepository", 11-05-2026
 */


/**
 * Owner-side creation, separate from LotteryRepository so existing waiting-list
 * and draw fakes do not need another method. Both use the same lotteries collection.
 */
interface LotteryCreationRepository {
    /** Saves a new lottery and returns its generated document ID after success. */
    // US 02.04.01: Declare owner-side creation without adding a method to the existing LotteryRepository interface.
    suspend fun createLottery(lottery: Lottery): String
}

/** Exact creation schema expected by the waiting-list and reservation code. */
// US 02.04.01 / US 02.04.03 / US 02.04.04 / US 02.04.05 / US 02.04.07: Map settings to the existing Firestore schema.
fun newLotteryToMap(lottery: Lottery): Map<String, Any> {
    LotteryController.validateCreation(lottery)
    return mutableMapOf<String, Any>(
        "campsiteId" to lottery.campsiteId,
        "campsiteName" to lottery.campsiteName,
        "ownerId" to lottery.ownerId,
        "registrationStart" to lottery.registrationStart,
        "registrationEnd" to lottery.registrationEnd,
        "bookingStart" to lottery.bookingStart,
        "bookingEnd" to lottery.bookingEnd,
        "winnerCount" to lottery.winnerCount,
        "waitingListCount" to 0,
        "status" to Lottery.STATUS_OPEN,
    ).apply {
        // Omission reads back as null (unlimited) in lotteryFromMap.
        lottery.waitlistCapacity?.let { put("waitlistCapacity", it) }
    }
}
