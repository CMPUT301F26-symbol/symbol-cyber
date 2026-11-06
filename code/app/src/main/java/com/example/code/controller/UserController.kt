/*
 * UserController.kt
 * Loads the device-based user profile and manages editable profile information.
 * Supports device identification and profile updates.
 */

package com.example.code.controller

import com.example.code.User
import com.example.code.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls loading and updating the user profile associated with the current device.
 *
 * @property repository data source used to load, create, and update users.
 */
class UserController(
    private val repository: UserRepository,
) {

    private val _currentUser = MutableStateFlow<User?>(null)

    /** The currently loaded user, or null before loading succeeds. */
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    /** Whether a user profile is currently being loaded or saved. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    /** Most recent user-loading or profile-saving error. */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Loads the profile associated with [deviceId].
     *
     * If no existing profile is found, a new default Entrant profile is
     * created for the device.
     */
    suspend fun loadOrCreateUser(deviceId: String) {
        if (deviceId.isBlank()) {
            _errorMessage.value = "Device identity is unavailable."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        try {
            val existingUser = repository.getUser(deviceId)

            _currentUser.value =
                existingUser ?: repository.createUser(deviceId)
        } catch (exception: Exception) {
            _errorMessage.value =
                "Failed to load user profile: ${exception.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Updates the current user's name, email, and phone number.
     *
     * @return true when the profile is valid and saved successfully,
     * false otherwise.
     */
    suspend fun updateProfile(
        name: String,
        email: String,
        phoneNumber: String,
    ): Boolean {
        val currentUser = _currentUser.value ?: return false

        if (!isValidEmail(email)) {
            _errorMessage.value = "Please enter a valid email address."
            return false
        }

        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val updatedUser =
                User(
                    deviceId = currentUser.deviceId,
                    name = name.trim(),
                    email = email.trim(),
                    phoneNumber = phoneNumber.trim(),
                    profilePhotoUrl = currentUser.profilePhotoUrl,
                    notificationsEnabled = currentUser.notificationsEnabled,
                    roles = currentUser.roles,
                )

            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser

            true
        } catch (exception: Exception) {
            _errorMessage.value =
                "Failed to save profile: ${exception.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val trimmedEmail = email.trim()

        return trimmedEmail.contains("@") &&
                trimmedEmail.substringAfter("@").contains(".")
    }
}