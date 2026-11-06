/*
 * AdminProfileRepository.kt
 * Lists stored user profiles and removes a selected profile document after confirmation.
 * Known issues: production access requires the reviewed Firestore admin rules; this code deploys no rules.
 */
package com.example.code.admin

import com.example.code.data.awaitResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Minimal administrative view, compatible with users/{deviceId} without changing User.
 * @property id authoritative Firestore document ID, not a client-supplied field.
 * @property name display name, possibly blank for a newly recognized device.
 * @property email contact address, if provided.
 * @property phone phone number, if provided.
 */
data class AdminProfile(val id: String, val name: String = "", val email: String = "", val phone: String = "")

/** Persistence boundary for profile removal; acceptance tests use an in-memory implementation. */
interface AdminProfileRepository {
    /** Emits profiles after changes; failures terminate the stream so the UI can offer retry. */
    fun observeProfiles(): Flow<List<AdminProfile>>
    /** Removes only [profileId]'s profile document; returns false if already absent. */
    suspend fun removeProfile(profileId: String): Boolean
}

/** Firestore implementation; server rules must enforce the administrator custom claim. */
class FirestoreAdminProfileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AdminProfileRepository {
    override fun observeProfiles(): Flow<List<AdminProfile>> = callbackFlow {
        val listener = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val rows = snapshot?.documents.orEmpty().mapNotNull { doc ->
                val fields = doc.data ?: return@mapNotNull null
                if (fields["isActive"] == false || fields["deletedAt"] != null) return@mapNotNull null
                AdminProfile(
                    doc.id,
                    fields["name"] as? String ?: "",
                    fields["email"] as? String ?: "",
                    fields["phoneNumber"] as? String ?: "",
                )
            }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.ifBlank { it.id } })
            trySend(rows)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun removeProfile(profileId: String): Boolean {
        val target = db.collection("users").document(profileId)
        return db.runTransaction { transaction ->
            if (!transaction.get(target).exists()) false
            else { transaction.delete(target); true }
        }.awaitResult()
    }
}
