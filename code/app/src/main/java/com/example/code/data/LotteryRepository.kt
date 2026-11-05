/*
 * LotteryRepository.kt
 * Reads lotteries and their waiting lists, adds or removes entrants, and
 * records the result of a draw.
 * The interface lets controllers and tests use a fake instead of Firestore;
 * FirestoreLotteryRepository is the real implementation.
 * Known issues: lotteries are listed from one collection without paging.
 */

package com.example.code.data

import android.util.Log
import com.example.code.controller.LotteryController
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


/** Result of trying to join a waiting list. */
enum class JoinResult {
    /** The entrant was added. */
    JOINED,

    /** The entrant was already on the waiting list. */
    ALREADY_JOINED,

    /** The waiting list has reached its capacity. */
    FULL,

    /** Registration is not open. */
    CLOSED,
}

/** Access to lotteries and their waiting lists. */
interface LotteryRepository {

    /** Emits all lotteries whenever any of them changes. */
    fun observeLotteries(): Flow<List<Lottery>>

    /** Emits the waiting list of lottery [lotteryId] whenever it changes. */
    fun observeWaitingList(lotteryId: String): Flow<List<WaitingListEntry>>

    /**
     * Adds [entry] to the waiting list of [lotteryId], respecting the
     * lottery's capacity.
     */
    suspend fun joinWaitingList(lotteryId: String, entry: WaitingListEntry): JoinResult

    /**
     * Removes entrant [entrantId] from the waiting list of [lotteryId].
     *
     * @return true if the entrant was on the list.
     */
    suspend fun leaveWaitingList(lotteryId: String, entrantId: String): Boolean

    /** Returns the current waiting list of [lotteryId] once. */
    suspend fun getWaitingList(lotteryId: String): List<WaitingListEntry>

    /**
     * Marks [lotteryId] as drawn and sets each entrant's status, only if the
     * lottery is still open.
     *
     * @return true if this call performed the draw; false if it was already drawn.
     */
    suspend fun completeDraw(
        lotteryId: String,
        winnerIds: List<String>,
        notSelectedIds: List<String>,
        drawnAt: Long,
    ): Boolean
}

/**
 * [LotteryRepository] backed by Cloud Firestore.
 *
 * Collections: `lotteries/{lotteryId}` and
 * `lotteries/{lotteryId}/waitingList/{entrantId}`.
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

    override fun observeWaitingList(lotteryId: String): Flow<List<WaitingListEntry>> =
        callbackFlow {
            val registration = lotteries.document(lotteryId)
                .collection(WAITING_LIST)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Failed to load waiting list $lotteryId", error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents.orEmpty().mapNotNull { document ->
                        document.data?.let { entryFromMap(document.id, it) }
                    }
                    trySend(list.sortedBy { it.joinedAt })
                }
            awaitClose { registration.remove() }
        }

    override suspend fun joinWaitingList(
        lotteryId: String,
        entry: WaitingListEntry,
    ): JoinResult {
        val lotteryRef = lotteries.document(lotteryId)
        val entryRef = lotteryRef.collection(WAITING_LIST).document(entry.entrantId)
        // Transaction: the capacity check and the count update happen together,
        // so two entrants cannot both take the last spot.
        return db.runTransaction { transaction ->
            val lotterySnapshot = transaction.get(lotteryRef)
            val lottery = lotteryFromMap(lotteryId, lotterySnapshot.data ?: emptyMap())
            when {
                transaction.get(entryRef).exists() -> JoinResult.ALREADY_JOINED
                !LotteryController.hasRoom(lottery) -> JoinResult.FULL
                else -> {
                    transaction.set(entryRef, entryToMap(entry))
                    transaction.update(lotteryRef, "waitingListCount", FieldValue.increment(1))
                    JoinResult.JOINED
                }
            }
        }.awaitResult()
    }

    override suspend fun leaveWaitingList(lotteryId: String, entrantId: String): Boolean {
        val lotteryRef = lotteries.document(lotteryId)
        val entryRef = lotteryRef.collection(WAITING_LIST).document(entrantId)
        return db.runTransaction { transaction ->
            if (!transaction.get(entryRef).exists()) {
                false
            } else {
                transaction.delete(entryRef)
                transaction.update(lotteryRef, "waitingListCount", FieldValue.increment(-1))
                true
            }
        }.awaitResult()
    }

    override suspend fun getWaitingList(lotteryId: String): List<WaitingListEntry> =
        lotteries.document(lotteryId).collection(WAITING_LIST).get().awaitResult()
            .documents.mapNotNull { document ->
                document.data?.let { entryFromMap(document.id, it) }
            }

    override suspend fun completeDraw(
        lotteryId: String,
        winnerIds: List<String>,
        notSelectedIds: List<String>,
        drawnAt: Long,
    ): Boolean {
        val lotteryRef = lotteries.document(lotteryId)
        val waitingList = lotteryRef.collection(WAITING_LIST)
        // Transaction: if two devices try to draw at once, only the first one
        // sees status OPEN; the second returns false and changes nothing.
        return db.runTransaction { transaction ->
            val status = transaction.get(lotteryRef).getString("status") ?: Lottery.STATUS_OPEN
            if (status != Lottery.STATUS_OPEN) {
                false
            } else {
                transaction.update(
                    lotteryRef,
                    mapOf("status" to Lottery.STATUS_DRAWN, "drawnAt" to drawnAt),
                )
                winnerIds.forEach {
                    transaction.update(waitingList.document(it), "status", WaitingListEntry.STATUS_WON)
                }
                notSelectedIds.forEach {
                    transaction.update(
                        waitingList.document(it),
                        "status",
                        WaitingListEntry.STATUS_NOT_SELECTED,
                    )
                }
                true
            }
        }.awaitResult()
    }

    companion object {
        /** Name of the lotteries collection. */
        const val LOTTERIES = "lotteries"

        /** Name of each lottery's waiting list subcollection. */
        const val WAITING_LIST = "waitingList"

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

/** Builds a [WaitingListEntry] from Firestore document [data]. */
fun entryFromMap(id: String, data: Map<String, Any?>): WaitingListEntry = WaitingListEntry(
    entrantId = id,
    entrantName = data["entrantName"] as? String ?: "",
    joinedAt = (data["joinedAt"] as? Number)?.toLong() ?: 0L,
    status = data["status"] as? String ?: WaitingListEntry.STATUS_WAITING,
)

/** Converts [entry] to the fields stored in Firestore. */
fun entryToMap(entry: WaitingListEntry): Map<String, Any> = mapOf(
    "entrantName" to entry.entrantName,
    "joinedAt" to entry.joinedAt,
    "status" to entry.status,
)
