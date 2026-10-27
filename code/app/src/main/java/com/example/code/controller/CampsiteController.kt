/*
 * CampsiteController.kt
 * Manages campsite data shared between owner and entrant screens.
 * Known issues: campsites are currently stored only in memory; Firestore
 * persistence will replace the temporary in-memory storage.
 */

package com.example.code.controller

import androidx.lifecycle.ViewModel
import com.example.code.model.Campsite
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls the campsite collection used by the application.
 */
class CampsiteController : ViewModel() {

    private val _campsites = MutableStateFlow<List<Campsite>>(emptyList())

    /**
     * Current campsites available in the application.
     */
    val campsites: StateFlow<List<Campsite>> = _campsites.asStateFlow()

    /**
     * Adds [campsite] to the current campsite collection.
     *
     * A temporary unique ID is generated when the campsite does not already
     * have one. Firestore document IDs will replace this behavior later.
     */
    fun addCampsite(campsite: Campsite) {
        val campsiteWithId =
            if (campsite.id.isBlank()) {
                campsite.copy(id = UUID.randomUUID().toString())
            } else {
                campsite
            }

        _campsites.value += campsiteWithId
    }
}