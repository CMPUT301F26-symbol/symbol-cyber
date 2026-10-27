/*
 * ModeController.kt
 * Controller that keeps the active app mode and checks which modes the
 * current user may switch to (US 03.11.01: an admin can also be an owner or
 * an entrant). Survives screen rotation because it is a ViewModel.
 * Known issues: by default all three modes are available; once the User
 * model (T3) exists, pass the user's roles in instead.
 */
package com.example.code.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the active [AppMode] and the modes the user is allowed to use.
 *
 * @param availableModes modes this user may switch to; must not be empty.
 * @param initialMode mode to start in; must be one of [availableModes].
 * @throws IllegalArgumentException if [availableModes] is empty or does not
 * contain [initialMode].
 */
class ModeController(
    availableModes: Set<AppMode> = AppMode.entries.toSet(),
    initialMode: AppMode = AppMode.ENTRANT,
) : ViewModel() {

    /** Modes this user may switch to, in [AppMode] order. */
    val availableModes: List<AppMode>

    private val _activeMode: MutableStateFlow<AppMode>

    /** The mode the app is in now. */
    val activeMode: StateFlow<AppMode>

    init {
        require(availableModes.isNotEmpty()) { "User must have at least one mode" }
        require(initialMode in availableModes) { "$initialMode is not available to this user" }
        this.availableModes = AppMode.entries.filter { it in availableModes }
        _activeMode = MutableStateFlow(initialMode)
        activeMode = _activeMode.asStateFlow()
    }

    /**
     * Returns true if the user may switch to [mode].
     */
    fun canSwitchTo(mode: AppMode): Boolean = mode in availableModes

    /**
     * Makes [mode] the active mode.
     *
     * @return true if the mode changed, false if it was already active.
     * @throws IllegalArgumentException if the user may not use [mode].
     */
    fun switchTo(mode: AppMode): Boolean {
        require(canSwitchTo(mode)) { "$mode is not available to this user" }
        if (_activeMode.value == mode) return false
        _activeMode.value = mode
        return true
    }

    /**
     * Returns the bottom bar tabs of the active mode.
     */
    fun tabs(): List<Destination> = Destination.tabsFor(_activeMode.value)

    /**
     * Returns the route of the first screen of the active mode.
     */
    fun startRoute(): String = Destination.startFor(_activeMode.value).route
}
