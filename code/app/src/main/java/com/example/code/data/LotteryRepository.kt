/*
 * LotteryRepository.kt
 * Reads lotteries from Firestore (lotteries/{id}). The interface lets
 * controllers and tests use a fake instead of Firestore;
 * FirestoreLotteryRepository is the real implementation.
 * Known issues: lotteries are listed from one collection without paging.
 */

package com.example.code.data

import android.util.Log
import com.example.code.model.Lottery
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Access to lotteries. */
interface LotteryRepository {

    /** Emits all lotteries whenever any of them changes. */
    fun observeLotteries(): Flow<List<Lottery>>
}

/**
 * [LotteryRepository] backed by Cloud Firestore.
 *
 * Collection: `lotteries/{lotteryId}`.
 */
class FirestoreLotteryRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : LotteryRepository {

    private val lotteries = db.collection(LOTTERIES)

    override fun observeLotteries(): Flow<List<Lottery>> = callbackFlow {
        val registration = lotteries.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Failed to load lotteries", error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents.orEmpty().mapNotNull { document ->
                document.data?.let { lotteryFromMap(document.id, it) }
            }
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    companion object {
        /** Name of the lotteries collection. */
        const val LOTTERIES = "lotteries"

        private const val TAG = "LotteryRepository"
    }
}

/**
 * Builds a [Lottery] from Firestore document [data]. Missing fields get the
 * model's defaults, so incomplete test documents do not crash the app.
 */
fun lotteryFromMap(id: String, data: Map<String, Any?>): Lottery = Lottery(
    id = id,
    campsiteId = data["campsiteId"] as? String ?: "",
    campsiteName = data["campsiteName"] as? String ?: "",
    ownerId = data["ownerId"] as? String ?: "",
    registrationStart = (data["registrationStart"] as? Number)?.toLong() ?: 0L,
    registrationEnd = (data["registrationEnd"] as? Number)?.toLong() ?: 0L,
    bookingStart = (data["bookingStart"] as? Number)?.toLong() ?: 0L,
    bookingEnd = (data["bookingEnd"] as? Number)?.toLong() ?: 0L,
    winnerCount = (data["winnerCount"] as? Number)?.toInt() ?: 1,
    waitlistCapacity = (data["waitlistCapacity"] as? Number)?.toInt(),
    waitingListCount = (data["waitingListCount"] as? Number)?.toInt() ?: 0,
    status = data["status"] as? String ?: Lottery.STATUS_OPEN,
)
