/*
 * ModeController.kt
 * Controller that keeps the active app mode and checks which modes the
 * current user may switch to.
 *
 * Available modes can be updated after the device-based User profile loads.
 */

package com.example.code.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the active [AppMode] and the modes the user is allowed to use.
 *
 * @param availableModes modes this user may initially switch to.
 * @param initialMode mode to start in; must be one of [availableModes].
 */
class ModeController(
    availableModes: Set<AppMode> = setOf(AppMode.ENTRANT),
    initialMode: AppMode = AppMode.ENTRANT,
) : ViewModel() {

    private val _availableModes =
        MutableStateFlow<List<AppMode>>(emptyList())

    /** Modes this user may switch to, in [AppMode] order. */
    val availableModes: StateFlow<List<AppMode>> =
        _availableModes.asStateFlow()

    private val _activeMode =
        MutableStateFlow(initialMode)

    /** The mode the app is currently using. */
    val activeMode: StateFlow<AppMode> =
        _activeMode.asStateFlow()

    init {
        require(availableModes.isNotEmpty()) {
            "User must have at least one mode"
        }

        require(initialMode in availableModes) {
            "$initialMode is not available to this user"
        }

        _availableModes.value =
            AppMode.entries.filter {
                it in availableModes
            }
    }

    /**
     * Replaces the modes available to the loaded user.
     *
     * If the current mode is no longer available, the first available mode
     * becomes active.
     */
    fun setAvailableModes(
        modes: Set<AppMode>,
    ) {
        require(modes.isNotEmpty()) {
            "User must have at least one mode"
        }

        val orderedModes =
            AppMode.entries.filter {
                it in modes
            }

        _availableModes.value = orderedModes

        if (_activeMode.value !in orderedModes) {
            _activeMode.value = orderedModes.first()
        }
    }

    /**
     * Returns true if the user may switch to [mode].
     */
    fun canSwitchTo(
        mode: AppMode,
    ): Boolean {
        return mode in _availableModes.value
    }

    /**
     * Makes [mode] the active mode.
     *
     * @return true if the mode changed, false if it was already active.
     * @throws IllegalArgumentException if the user may not use [mode].
     */
    fun switchTo(
        mode: AppMode,
    ): Boolean {
        require(canSwitchTo(mode)) {
            "$mode is not available to this user"
        }

        if (_activeMode.value == mode) {
            return false
        }

        _activeMode.value = mode
        return true
    }

    /**
     * Returns the bottom bar tabs of the active mode.
     */
    fun tabs(): List<Destination> {
        return Destination.tabsFor(
            _activeMode.value,
        )
    }

    /**
     * Returns the route of the first screen of the active mode.
     */
    fun startRoute(): String {
        return Destination.startFor(
            _activeMode.value,
        ).route
    }
}