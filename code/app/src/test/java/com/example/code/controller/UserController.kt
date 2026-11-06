/*
 * UserController.kt
 * Loads the current device-based user profile and exposes it to the app.
 * Supports US 01.09.01 device-based identification.
 */

package com.example.code.controller

import com.example.code.User
import com.example.code.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls loading of the user profile associated with the current device.
 *
 * @property repository data source used to load or create users.
 */
class UserController(
    private val repository: UserRepository,
) {

    private val _currentUser = MutableStateFlow<User?>(null)

    /** The currently loaded user, or null before loading succeeds. */
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    /** Whether a user profile is currently being loaded or created. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    /** Most recent user-loading error, or null when no error is present. */
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
}