// Loads campsite ratings from Firestore and lets the admin remove one. Known issue is that entrants can't give ratings in the app yet, so ratings are added in the Firebase console for now.
package com.example.code.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.code.model.Rating
import com.example.code.model.averageRating
import com.example.code.model.ratingsForCampsite
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    private val campsiteCollection = db.collection("campsites")

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
        // The ratings the campsite keeps after this one is removed
        val remaining = ratingsForCampsite(_ratings.value, rating.campsiteId)
            .filter { it.id != rating.id }

        val batch = db.batch()
        batch.delete(ratingCollection.document(rating.id))

        // Old test campsites keep their score under averageRating, so clear that too
        batch.update(
            campsiteCollection.document(rating.campsiteId),
            "rating", averageRating(remaining),
            "averageRating", FieldValue.delete(),
        )

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("Could not remove rating. Try again.") }
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
    )
}
