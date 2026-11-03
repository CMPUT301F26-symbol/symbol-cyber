/*
 * ReservationRepository.kt
 * Reads and saves reservations (Firestore: reservations/{id}). The interface
 * lets controllers and tests use a fake instead of Firestore.
 * Known issues: all reservations are read and filtered on the device.
 */

package com.example.code.data

import android.util.Log
import com.example.code.model.Reservation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Access to reservations. */
interface ReservationRepository {

    /** Emits all reservations whenever any of them changes. */
    fun observeReservations(): Flow<List<Reservation>>

    /**
     * Saves [reservation] under its [Reservation.id].
     *
     * @return false if a reservation with that ID already exists.
     */
    suspend fun create(reservation: Reservation): Boolean
}

/** [ReservationRepository] backed by Cloud Firestore. */
class FirestoreReservationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ReservationRepository {

    private val reservations = db.collection(RESERVATIONS)

    override fun observeReservations(): Flow<List<Reservation>> = callbackFlow {
        val registration = reservations.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Failed to load reservations", error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents.orEmpty().mapNotNull { document ->
                document.data?.let { reservationFromMap(document.id, it) }
            }
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun create(reservation: Reservation): Boolean {
        val ref = reservations.document(reservation.id)
        return db.runTransaction { transaction ->
            if (transaction.get(ref).exists()) {
                false
            } else {
                transaction.set(ref, reservationToMap(reservation))
                true
            }
        }.awaitResult()
    }

    companion object {
        /** Name of the reservations collection. */
        const val RESERVATIONS = "reservations"

        private const val TAG = "ReservationRepository"
    }
}

/** Builds a [Reservation] from Firestore document [data]. */
fun reservationFromMap(id: String, data: Map<String, Any?>): Reservation = Reservation(
    id = id,
    lotteryId = data["lotteryId"] as? String ?: "",
    campsiteId = data["campsiteId"] as? String ?: "",
    campsiteName = data["campsiteName"] as? String ?: "",
    entrantId = data["entrantId"] as? String ?: "",
    entrantName = data["entrantName"] as? String ?: "",
    ownerId = data["ownerId"] as? String ?: "",
    startDate = (data["startDate"] as? Number)?.toLong() ?: 0L,
    endDate = (data["endDate"] as? Number)?.toLong() ?: 0L,
    status = data["status"] as? String ?: Reservation.STATUS_ACTIVE,
    rejectionReason = data["rejectionReason"] as? String ?: "",
    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
)

/** Converts [reservation] to the fields stored in Firestore. */
fun reservationToMap(reservation: Reservation): Map<String, Any> = mapOf(
    "lotteryId" to reservation.lotteryId,
    "campsiteId" to reservation.campsiteId,
    "campsiteName" to reservation.campsiteName,
    "entrantId" to reservation.entrantId,
    "entrantName" to reservation.entrantName,
    "ownerId" to reservation.ownerId,
    "startDate" to reservation.startDate,
    "endDate" to reservation.endDate,
    "status" to reservation.status,
    "rejectionReason" to reservation.rejectionReason,
    "createdAt" to reservation.createdAt,
)
