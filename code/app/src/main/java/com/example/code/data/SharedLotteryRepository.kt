// Saves the campsites owners add to or remove from a shared lottery in Firestore.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.data

import com.example.code.controller.SharedLotteryController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Saves changes to the campsites in a shared lottery.
 *
 * Tests can use a fake instead of Firestore.
 */
interface SharedLotteryRepository {

    /**
     * Adds an owner's campsite to a lottery.
     *
     * @param lotteryId the id of the lottery
     * @param campsite the campsite to add
     * @param ownerId the device id of the owner adding it
     */
    suspend fun addCampsite(lotteryId: String, campsite: Campsite, ownerId: String)

    /**
     * Removes an owner's campsite from a lottery.
     *
     * @param lotteryId the id of the lottery
     * @param campsite the campsite to remove
     * @param ownerId the device id of the owner removing it
     */
    suspend fun removeCampsite(lotteryId: String, campsite: Campsite, ownerId: String)
}

/**
 * [SharedLotteryRepository] backed by the team's lotteries collection in Firestore.
 *
 * @param db the Firestore database to use
 */
class FirestoreSharedLotteryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : SharedLotteryRepository {

    private val lotteries = db.collection(FirestoreLotteryRepository.LOTTERIES)

    override suspend fun addCampsite(lotteryId: String, campsite: Campsite, ownerId: String) {
        saveChange(lotteryId) { lottery ->
            SharedLotteryController.addCampsite(lottery, campsite, ownerId, System.currentTimeMillis())
        }
    }

    override suspend fun removeCampsite(lotteryId: String, campsite: Campsite, ownerId: String) {
        saveChange(lotteryId) { lottery ->
            SharedLotteryController.removeCampsite(lottery, campsite, ownerId, System.currentTimeMillis())
        }
    }

    /**
     * Reads the latest lottery, applies the change and saves its campsites.
     *
     * It all happens in one transaction, so two owners changing the same lottery at once don't undo each other.
     *
     * @param lotteryId the id of the lottery
     * @param change turns the latest lottery into the changed one
     */
    private suspend fun saveChange(lotteryId: String, change: (Lottery) -> Lottery) {
        val ref = lotteries.document(lotteryId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            require(snapshot.exists()) { "This lottery no longer exists." }

            val updated = change(lotteryFromMap(snapshot.id, snapshot.data.orEmpty()))

            // Only the campsite fields change, the waiting list and dates are left alone
            transaction.update(
                ref,
                mapOf(
                    "campsiteId" to updated.campsiteId,
                    "campsiteName" to updated.campsiteName,
                    "campsites" to updated.campsites.map { mapOf("id" to it.id, "name" to it.name) },
                ),
            )
        }.awaitResult()
    }
}
