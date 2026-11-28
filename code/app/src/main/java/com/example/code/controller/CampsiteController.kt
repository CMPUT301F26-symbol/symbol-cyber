/*
 * CampsiteController.kt
 * Manages campsite data shared between owner and entrant screens.
 * Campsites are persisted in Cloud Firestore and exposed to the UI as StateFlow.
 * Known issue is that updating a campsite rewrites the whole document, so old fields like amenities on very old test campsites are lost.
 */

package com.example.code.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.code.admin.FirebaseAdminAuthorization
import com.example.code.data.awaitResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
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
     * Removes a campsite from Firestore.
     *
     * Once removed, it disappears from every campsite list in the app.
     *
     * @param campsite the campsite to remove
     * @param onSuccess called once Firestore has removed the campsite
     * @param onFailure called with an error message if the campsite was not removed
     */
    fun removeCampsite(campsite: Campsite, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        campsiteCollection.document(campsite.id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("Could not remove campsite. Try again.") }
    }

    /**
     * Removes the image from a campsite in Firestore, keeping the rest of the campsite.
     *
     * Once removed, the image disappears from the admin images list and the campsite details.
     *
     * @param campsite the campsite whose image should be removed
     * @param onSuccess called once Firestore has removed the image
     * @param onFailure called with an error message if the image was not removed
     */
    fun removeCampsiteImage(campsite: Campsite, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val expectedImage = campsite.imageUrl
        if (campsite.id.isBlank() || '/' in campsite.id || expectedImage.isNullOrBlank()) {
            onFailure("Select a saved campsite image.")
            return
        }
        viewModelScope.launch {
            try {
                if (!FirebaseAdminAuthorization().isAdmin()) {
                    onFailure("Verified administrator access is required to remove images.")
                    return@launch
                }
                val ref = campsiteCollection.document(campsite.id)
                val removed = db.runTransaction { transaction ->
                    val record = transaction.get(ref)
                    if (!record.exists() || record.getString("imageUrl") != expectedImage) {
                        return@runTransaction false
                    }
                    transaction.update(ref, "imageUrl", null)
                    true
                }.awaitResult()
                if (removed) onSuccess()
                else onFailure("This image changed or was already removed. Refresh the list and try again.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                onFailure("Could not remove image. Try again.")
            }
        }
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
     * Saves an owner's changes to an existing campsite in Firestore.
     *
     * The whole campsite is saved again under its id, so the change shows
     * everywhere the campsite is listed.
     *
     * @param campsite the edited campsite, with its id and owner kept
     * @param onSuccess called once Firestore has saved the changes
     * @param onFailure called with an error message if the changes were not saved
     */
    fun updateCampsite(campsite: Campsite, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Can't update a campsite that was never saved
        if (campsite.id.isBlank()) {
            onFailure("Could not save changes. Try again.")
            return
        }

        // The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.13.01. Make sure to look through existing code and add all UI elements, logic, and test cases", November 27, 2026.
        // Owner edits now update only listing fields. The earlier full-document
        // description is historical: entrant scores and aggregate totals stay intact.
        campsiteCollection.document(campsite.id).update(campsiteOwnerUpdateToMap(campsite))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("Could not save changes. Try again.") }
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
