/*
 * AdminProfileController.kt
 * Verifies administrator rights before listing/removing profiles and protects the active profile.
 * Known issues: an admin custom claim must be provisioned by the Firebase administrator, never by this app.
 */
package com.example.code.admin

import com.example.code.data.awaitResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/** Server-verifiable authorization boundary, replaceable in tests. */
fun interface AdminAuthorization {
    /** Returns whether the currently authenticated session is an administrator. */
    suspend fun isAdmin(): Boolean
}

/** Uses a refreshed, server-issued Firebase custom claim rather than trusting the mode switch. */
class FirebaseAdminAuthorization(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AdminAuthorization {
    override suspend fun isAdmin(): Boolean =
        auth.currentUser?.getIdToken(true)?.awaitResult()?.claims?.get("admin") == true
}

/**
 * Administrative profile list state.
 * @property loading waiting for authorization or the first snapshot.
 * @property authorized true only after administrator verification.
 * @property profiles currently available profiles.
 * @property error safe user-facing failure, or null.
 */
data class AdminProfilesState(
    val loading: Boolean = true,
    val authorized: Boolean = false,
    val profiles: List<AdminProfile> = emptyList(),
    val error: String? = null,
)

/** Result of a confirmed removal. */
enum class AdminRemovalResult { REMOVED, ALREADY_REMOVED, DENIED, INVALID, FAILED }

/**
 * Coordinates administrator authorization and profile persistence.
 * @param repository existing user profile storage.
 * @param authorization verification for the currently authenticated administrator.
 * @param currentProfileId active user's device/profile ID; self-removal belongs to a separate flow.
 */
class AdminProfileController(
    private val repository: AdminProfileRepository,
    private val authorization: AdminAuthorization,
    private val currentProfileId: String,
) {
    /** Observes profiles only after authorization; errors clear the list and support a new subscription. */
    fun observe(): Flow<AdminProfilesState> = flow {
        emit(AdminProfilesState())
        if (!authorization.isAdmin()) { emit(AdminProfilesState(loading = false)); return@flow }
        repository.observeProfiles().collect { emit(AdminProfilesState(false, true, it)) }
    }.catch { failure ->
        if (failure is CancellationException) throw failure
        emit(AdminProfilesState(loading = false, error = "Could not load profiles or verify administrator access. Try again."))
    }

    /**
     * Removes a confirmed [profileId] after a fresh authorization check.
     * A missing identity, invalid ID or self-removal cannot reach persistence.
     */
    suspend fun remove(profileId: String): AdminRemovalResult {
        if (currentProfileId.isBlank() || profileId.isBlank() || '/' in profileId || profileId == currentProfileId) return AdminRemovalResult.INVALID
        return try {
            if (!authorization.isAdmin()) AdminRemovalResult.DENIED
            else if (repository.removeProfile(profileId)) AdminRemovalResult.REMOVED
            else AdminRemovalResult.ALREADY_REMOVED
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AdminRemovalResult.FAILED
        }
    }
}
