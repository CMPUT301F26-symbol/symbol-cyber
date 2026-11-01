/*
 * CampsiteController.kt
 * Manages campsite data shared between owner and entrant screens.
 * Campsites are persisted in Cloud Firestore and exposed to the UI as StateFlow.
 * Known issues: owner-specific filtering and richer error UI are not implemented yet.
 */

package com.example.code.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.code.model.Campsite
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
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

    init {
        listenForCampsites()
    }

    /**
     * Adds [campsite] to Firestore.
     *
     * Firestore generates the campsite ID. The generated ID is also stored
     * inside the campsite document.
     */
    fun addCampsite(campsite: Campsite) {
        val documentReference = campsiteCollection.document()

        val campsiteWithId = campsite.copy(
            id = documentReference.id
        )

        documentReference
            .set(campsiteWithId)
            .addOnSuccessListener {
                _errorMessage.value = null

                Log.d(
                    "CampsiteController",
                    "Campsite saved with ID ${documentReference.id}"
                )
            }
            .addOnFailureListener { exception ->
                _errorMessage.value =
                    "Failed to save campsite: ${exception.message}"

                Log.e(
                    "CampsiteController",
                    "Failed to save campsite",
                    exception
                )
            }
    }

    /**
     * Listens for campsite changes in Firestore and updates the app state.
     *
     * Existing test documents may use an older schema, so documents are
     * converted manually instead of directly using Firestore's toObject().
     */
    private fun listenForCampsites() {
        campsiteCollection.addSnapshotListener { snapshot, exception ->

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

                    val storedLocation = data["location"]

                    val locationText =
                        when (storedLocation) {
                            is String -> storedLocation

                            is GeoPoint ->
                                "${storedLocation.latitude}, ${storedLocation.longitude}"

                            else -> ""
                        }

                    val latitude =
                        (data["latitude"] as? Number)?.toDouble()
                            ?: (storedLocation as? GeoPoint)?.latitude
                            ?: 0.0

                    val longitude =
                        (data["longitude"] as? Number)?.toDouble()
                            ?: (storedLocation as? GeoPoint)?.longitude
                            ?: 0.0

                    val description =
                        data["description"] as? String
                            ?: data["amenities"] as? String
                            ?: ""

                    val rating =
                        (data["rating"] as? Number)?.toDouble()
                            ?: (data["averageRating"] as? Number)?.toDouble()

                    val maxOccupancy =
                        (data["maxOccupancy"] as? Number)?.toInt()
                            ?: (data["maximumOccupancy"] as? Number)?.toInt()
                            ?: 0

                    val tags =
                        (data["tags"] as? List<*>)
                            ?.filterIsInstance<String>()
                            ?: emptyList()

                    Campsite(
                        id = document.id,
                        ownerId = data["ownerId"] as? String ?: "",
                        name = data["name"] as? String ?: "",
                        location = locationText,
                        description = description,
                        maxOccupancy = maxOccupancy,
                        latitude = latitude,
                        longitude = longitude,
                        rating = rating,
                        imageUrl = data["imageUrl"] as? String,
                        tags = tags,
                    )
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