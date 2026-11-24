/*
 * UserRepository.kt
 * Loads, creates, and updates user profiles in Cloud Firestore using device ID.
 * Supports device-based identification and editable user profile information.
 */

package com.example.code.data

import com.example.code.User
import com.example.code.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Provides access to persisted user profiles.
 */
interface UserRepository {

    /**
     * Loads the user associated with [deviceId].
     *
     * @return the matching user, or null if no profile exists yet.
     */
    suspend fun getUser(deviceId: String): User?

    /**
     * Creates and stores a new default entrant profile for [deviceId].
     *
     * @return the newly created user.
     */
    suspend fun createUser(deviceId: String): User

    /**
     * Stores the current editable information for [user].
     */
    suspend fun updateUser(user: User)
}

/**
 * Firestore-backed implementation of [UserRepository].
 */
class FirestoreUserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : UserRepository {

    override suspend fun getUser(deviceId: String): User? {
        val snapshot =
            db.collection(USERS)
                .document(deviceId)
                .get(Source.SERVER)
                .awaitResult()

        if (!snapshot.exists()) return null
        check(isActive(snapshot)) { "This profile has been removed." }
        return toUser(snapshot, deviceId)
    }

    /** Observes committed changes to an existing profile while the app is open. */
    fun observeUser(deviceId: String): Flow<User?> = callbackFlow {
        val listener = db.collection(USERS).document(deviceId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(error)
                } else if (snapshot != null && !snapshot.metadata.isFromCache &&
                    !snapshot.metadata.hasPendingWrites()
                ) {
                    trySend(if (snapshot.exists() && isActive(snapshot)) toUser(snapshot, deviceId) else null)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun isActive(snapshot: DocumentSnapshot): Boolean =
        snapshot.getBoolean("isActive") != false && snapshot.get("deletedAt") == null

    private fun toUser(snapshot: DocumentSnapshot, deviceId: String): User {
        val roles =
            (snapshot.get("roles") as? List<*>)
                ?.filterIsInstance<String>()
                ?.mapNotNull { roleName ->
                    UserRole.entries.find { it.name == roleName }
                }
                ?.toSet()
                .orEmpty()
                .ifEmpty {
                    setOf(UserRole.ENTRANT)
                }

        return User(
            deviceId = deviceId,
            name = snapshot.getString("name") ?: "",
            email = snapshot.getString("email") ?: "",
            phoneNumber = snapshot.getString("phoneNumber") ?: "",
            profilePhotoUrl = snapshot.getString("profilePhotoUrl"),
            notificationsEnabled =
                snapshot.getBoolean("notificationsEnabled") ?: true,
            roles = roles,
        )
    }

    override suspend fun createUser(deviceId: String): User {
        val user =
            User(
                deviceId = deviceId,
                roles = setOf(UserRole.ENTRANT),
            )

        val data =
            mapOf(
                "deviceId" to user.deviceId,
                "name" to user.name,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "profilePhotoUrl" to user.profilePhotoUrl,
                "notificationsEnabled" to user.notificationsEnabled,
                "roles" to user.roles.map { it.name },
            )

        val ref = db.collection(USERS).document(deviceId)
        return db.runTransaction { transaction ->
            val existing = transaction.get(ref)
            if (existing.exists()) {
                check(isActive(existing)) { "This profile has been removed." }
                toUser(existing, deviceId)
            } else {
                transaction.set(ref, data)
                user
            }
        }.awaitResult()
    }

    override suspend fun updateUser(user: User) {
        val updates =
            mapOf(
                "name" to user.name,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "profilePhotoUrl" to user.profilePhotoUrl,
                "notificationsEnabled" to user.notificationsEnabled,
            )

        val ref = db.collection(USERS).document(user.deviceId)
        db.runTransaction { transaction ->
            val existing = transaction.get(ref)
            check(existing.exists() && isActive(existing)) { "This profile is unavailable." }
            transaction.update(ref, updates)
        }.awaitResult()
    }

    companion object {
        const val USERS = "users"
    }
}