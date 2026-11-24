// Loads campsite reviews from Firestore and lets the admin remove one. Known issue is that entrants can't post reviews in the app yet, so reviews are added in the Firebase console for now.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.code.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls campsite reviews in Firestore and provides them to the UI.
 *
 * @property reviews every review loaded from Firestore.
 * @property isLoading true until the reviews first load.
 * @property errorMessage the most recent loading error, or null if there is none.
 */
class ReviewController : ViewModel() {

    private val reviewCollection = FirebaseFirestore.getInstance().collection("reviews")

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var listener: ListenerRegistration? = null

    init {
        listenForReviews()
    }

    override fun onCleared() {
        listener?.remove()
    }

    /**
     * Starts loading the reviews again after an error.
     */
    fun retryLoading() {
        listener?.remove()
        _isLoading.value = true
        _errorMessage.value = null
        listenForReviews()
    }

    /**
     * Removes a review from Firestore.
     *
     * Once removed, it disappears everywhere the campsite's reviews are shown.
     *
     * @param review the review to remove
     * @param onSuccess called once Firestore has removed the review
     * @param onFailure called with an error message if the review was not removed
     */
    fun removeReview(review: Review, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        reviewCollection.document(review.id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("Could not remove review. Try again.") }
    }

    /**
     * Listens for review changes in Firestore and updates the app state.
     */
    private fun listenForReviews() {
        listener = reviewCollection.addSnapshotListener { snapshot, exception ->
            _isLoading.value = false

            if (exception != null) {
                _errorMessage.value = "Failed to load reviews: ${exception.message}"
                Log.e("ReviewController", "Failed to load reviews", exception)
                return@addSnapshotListener
            }

            if (snapshot == null) {
                return@addSnapshotListener
            }

            _reviews.value = snapshot.documents.mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                reviewFromDocument(document.id, data)
            }
            _errorMessage.value = null
        }
    }
}

/**
 * Turns a Firestore review document into a Review.
 *
 * @param id the document id
 * @param data the fields saved in the document
 * @return the review, or null if it doesn't say which campsite it belongs to
 */
internal fun reviewFromDocument(id: String, data: Map<String, Any?>): Review? {
    val campsiteId = data["campsiteId"] as? String

    // A review without a campsite can't be shown anywhere
    if (campsiteId.isNullOrBlank()) {
        return null
    }

    return Review(
        id = id,
        entrantId = data["entrantId"] as? String ?: "",
        campsiteId = campsiteId,
        text = data["text"] as? String ?: "",
    )
}
