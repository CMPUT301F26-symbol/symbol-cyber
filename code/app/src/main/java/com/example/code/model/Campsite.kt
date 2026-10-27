/*
 * Campsite.kt
 * Represents a campsite registered by an owner and viewed by entrants.
 * Known issues: Firestore persistence is not connected yet.
 */

package com.example.code.model

/**
 * Represents a campsite in the application.
 *
 * @property id unique campsite identifier.
 * @property ownerId identifier of the campsite owner.
 * @property name campsite name.
 * @property location human-readable campsite location.
 * @property description campsite description and amenities.
 * @property maxOccupancy maximum number of guests allowed.
 * @property rating average campsite rating, or null if there are no ratings yet.
 * @property imageUrl optional promotional image URL.
 * @property tags descriptive campsite tags.
 */
data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val maxOccupancy: Int = 0,
    val rating: Double? = null,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
)