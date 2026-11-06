/*
 * CampsiteController.kt
 * Manages campsite data shared between owner and entrant screens.
 * Campsites are persisted in Cloud Firestore and exposed to the UI as StateFlow.
 */

package com.example.code.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.code.model.Campsite
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls campsite persistence and provides campsite data to the UI.
 */
class CampsiteController : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val campsiteCollection = db.collection("campsites")

    private val _campsites = MutableStateFlow<List<Campsite>>(emptyList())

    /**
     * Current campsites loaded from Firestore.
     */
    val campsites: StateFlow<List<Campsite>> = _campsites.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)

    /**
     * Most recent Firestore error, or null if no error is present.
     */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var listener: ListenerRegistration? = null
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Restarts a failed subscription, preserving the screen's current search. */
    fun retryLoading() {
        listener?.remove()
        _isLoading.value = true
        _errorMessage.value = null
        listenForCampsites()
    }

    override fun onCleared() {
        listener?.remove()
    }

    init {
        listenForCampsites()
    }

    /**
     * Saves a new campsite to Firestore under its owner.
     *
     * If the campsite has no owner, or Firestore fails, the campsite is not saved.
     *
     * @param campsite the campsite filled in by the owner, with their device id as ownerId
     * @param onSuccess called once Firestore has saved the campsite
     * @param onFailure called with an error message if the campsite was not saved
     */
    fun addCampsite(campsite: Campsite, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Don't save a campsite that doesn't belong to anyone
        if (campsite.ownerId.isBlank()) {
            onFailure("Your device identity is unavailable.")
            return
        }

        val documentReference = campsiteCollection.document()

        // Add the new id to the campsite
        val campsiteToSave = campsite.copy(id = documentReference.id)

        documentReference.set(campsiteToSave)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("Could not save campsite. Try again.") }
    }

    /**
     * Listens for campsite changes in Firestore and updates the app state.
     *
     * Existing test documents may use an older schema, so documents are
     * converted manually instead of directly using Firestore's toObject().
     */
    private fun listenForCampsites() {
        listener = campsiteCollection.addSnapshotListener { snapshot, exception ->
            _isLoading.value = false

            if (exception != null) {
                _errorMessage.value =
                    "Failed to load campsites: ${exception.message}"

                Log.e(
                    "CampsiteController",
                    "Failed to load campsites",
                    exception
                )

                return@addSnapshotListener
            }

            if (snapshot == null) {
                return@addSnapshotListener
            }

            val loadedCampsites = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null

                    campsiteFromDocument(document.id, data)
                } catch (exception: Exception) {
                    Log.e(
                        "CampsiteController",
                        "Skipping invalid campsite document ${document.id}",
                        exception
                    )

                    null
                }
            }

            _campsites.value = loadedCampsites
            _errorMessage.value = null
        }
    }
}