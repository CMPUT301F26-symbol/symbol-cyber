/* Reads public campsite reviews and commits a review only against a verified past stay. */
package com.example.code.data

import com.example.code.model.CampsiteReview
import com.example.code.model.MAX_REVIEW_LENGTH
import com.example.code.model.Reservation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Access to a campsite's public reviews and completed-stay submission. */
interface CampsiteReviewStore {
    fun observe(campsiteId: String): Flow<List<CampsiteReview>>
    suspend fun post(campsiteId: String, reservationId: String, entrantId: String, text: String)
}

/** Stores one review per reservation. Server rules independently enforce eligibility. */
class CampsiteReviewRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) : CampsiteReviewStore {
    override fun observe(campsiteId: String): Flow<List<CampsiteReview>> = callbackFlow {
        val registration = reviews(campsiteId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val rows = snapshot?.documents.orEmpty().mapNotNull { document ->
                val body = document.getString("body")?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                CampsiteReview(
                    reservationId = document.id,
                    authorName = document.getString("authorName").orEmpty(),
                    body = body,
                    createdAt = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                )
            }.sortedWith(compareByDescending<CampsiteReview> { it.createdAt }.thenBy { it.reservationId })
            trySend(rows)
        }
        awaitClose { registration.remove() }
    }

    /** Rechecks the persisted reservation and profile immediately before writing. */
    override suspend fun post(campsiteId: String, reservationId: String, entrantId: String, text: String) {
        require(listOf(campsiteId, reservationId, entrantId).all { it.isNotBlank() && '/' !in it })
        val body = text.trim()
        require(body.isNotEmpty() && body.length <= MAX_REVIEW_LENGTH)
        val campsite = db.collection("campsites").document(campsiteId)
        val stay = db.collection("reservations").document(reservationId)
        val profile = db.collection(FirestoreUserRepository.USERS).document(entrantId)
        val review = reviews(campsiteId).document(reservationId)
        db.runTransaction { transaction ->
            val savedStay = transaction.get(stay)
            val endDate = savedStay.getLong("endDate") ?: 0L
            check(
                savedStay.getString("entrantId") == entrantId &&
                    savedStay.getString("campsiteId") == campsiteId &&
                    savedStay.getString("status") == Reservation.STATUS_ACTIVE &&
                    endDate in 1 until System.currentTimeMillis(),
            ) { "Only completed stays can be reviewed." }
            check(transaction.get(campsite).exists()) { "This campsite is unavailable." }
            val author = transaction.get(profile)
            check(author.exists() && author.getBoolean("isActive") != false && author.get("deletedAt") == null) {
                "Your profile is unavailable."
            }
            check(!transaction.get(review).exists()) { "This stay already has a review." }
            transaction.set(
                review,
                mapOf(
                    "authorName" to author.getString("name").orEmpty(),
                    "body" to body,
                    "createdAt" to FieldValue.serverTimestamp(),
                ),
            )
        }.awaitResult()
    }

    private fun reviews(campsiteId: String) =
        db.collection("campsites").document(campsiteId).collection("reviews")
}
