/*
 * UserRepository.kt
 * Loads and creates user profiles in Cloud Firestore using device ID.
 * This supports US 01.09.01 device-based identification.
 */

package com.example.code.data

import com.example.code.User
import com.example.code.UserRole
import com.google.firebase.firestore.FirebaseFirestore

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
                .get()
                .awaitResult()

        if (!snapshot.exists()) {
            return null
        }

        val roles =
            (snapshot.get("roles") as? List<*>)
                ?.filterIsInstance<String>()
                ?.mapNotNull { roleName ->
                    UserRole.entries.find { it.name == roleName }
                }
                ?.toSet()
                ?: setOf(UserRole.ENTRANT)

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

        db.collection(USERS)
            .document(deviceId)
            .set(data)
            .awaitResult()

        return user
    }

    companion object {
        const val USERS = "users"
    }
}