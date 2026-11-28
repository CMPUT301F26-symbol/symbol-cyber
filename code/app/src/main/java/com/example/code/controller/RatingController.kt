// Loads campsite ratings from Firestore and lets the admin remove one. Known issue is that entrants can't give ratings in the app yet, so ratings are added in the Firebase console for now.
package com.example.code.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.code.model.Rating
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
// The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.13.01. Make sure to look through existing code and add all UI elements, logic, and test cases", November 27, 2026.
import androidx.lifecycle.viewModelScope
import com.example.code.data.FirestoreCampsiteRatingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Controls campsite ratings in Firestore and provides them to the UI.
 *
 * @property ratings every rating loaded from Firestore.
 * @property isLoading true until the ratings first load.
 * @property errorMessage the most recent loading error, or null if there is none.
 */
class RatingController : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val ratingCollection = db.collection("ratings")
    // The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.13.01. Make sure to look through existing code and add all UI elements, logic, and test cases", November 27, 2026.
    // Entrant submissions now exist; the historical header above describes the previous implementation.
    private val ratingRepository = FirestoreCampsiteRatingRepository(db)

    private val _ratings = MutableStateFlow<List<Rating>>(emptyList())
    val ratings: StateFlow<List<Rating>> = _ratings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var listener: ListenerRegistration? = null

    init {
        listenForRatings()
    }

    override fun onCleared() {
        listener?.remove()
    }

    /**
     * Starts loading the ratings again after an error.
     */
    fun retryLoading() {
        listener?.remove()
        _isLoading.value = true
        _errorMessage.value = null
        listenForRatings()
    }

    /**
     * Removes a rating from Firestore and updates its campsite's overall rating.
     *
     * Both changes are saved together, so the overall rating never includes a removed rating.
     *
     * @param rating the rating to remove
     * @param onSuccess called once Firestore has removed the rating
     * @param onFailure called with an error message if the rating was not removed
     */
    fun removeRating(rating: Rating, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.13.01. Make sure to look through existing code and add all UI elements, logic, and test cases", November 27, 2026.
        // The repository reads the saved score and aggregate inside the same transaction.
        // This prevents a removal from overwriting a concurrent entrant submission.
        viewModelScope.launch {
            try {
                ratingRepository.removeRating(rating)
                onSuccess()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                onFailure("Could not remove rating. Try again.")
            }
        }
    }

    /**
     * Listens for rating changes in Firestore and updates the app state.
     */
    private fun listenForRatings() {
        listener = ratingCollection.addSnapshotListener { snapshot, exception ->
            _isLoading.value = false

            if (exception != null) {
                _errorMessage.value = "Failed to load ratings: ${exception.message}"
                Log.e("RatingController", "Failed to load ratings", exception)
                return@addSnapshotListener
            }

            if (snapshot == null) {
                return@addSnapshotListener
            }

            _ratings.value = snapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                ratingFromDocument(document.id, data)
            }
            _errorMessage.value = null
        }
    }
}

/**
 * Turns a Firestore rating document into a Rating.
 *
 * @param id the document id
 * @param data the fields saved in the document
 * @return the rating, or null if it doesn't say which campsite it belongs to
 */
internal fun ratingFromDocument(id: String, data: Map<String, Any?>): Rating? {
    val campsiteId = data["campsiteId"] as? String

    // A rating without a campsite can't be shown anywhere
    if (campsiteId.isNullOrBlank()) {
        return null
    }

    return Rating(
        id = id,
        entrantId = data["entrantId"] as? String ?: "",
        campsiteId = campsiteId,
        score = (data["score"] as? Number)?.toInt() ?: 0,
        // The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.13.01. Make sure to look through existing code and add all UI elements, logic, and test cases", November 27, 2026.
        reservationId = data["reservationId"] as? String ?: "",
    )
}

