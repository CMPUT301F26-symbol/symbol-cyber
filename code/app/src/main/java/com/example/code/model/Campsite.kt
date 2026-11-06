/*
 * Campsite.kt
 * Represents a campsite registered by an owner and viewed by entrants.
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
 * @property latitude latitude of the campsite's map location.
 * @property longitude longitude of the campsite's map location.
 * @property rating average campsite rating, or null if there are no ratings yet.
 * @property imageUrl optional promotional image URL.
 * @property tags descriptive campsite tags.
 * @property price optional advertised price; null when unspecified.
 * @property currency optional currency code stored with the price.
 * @property rules campsite rules supplied by the owner.
 * @property cancellationPolicy cancellation terms supplied by the owner.
 */
data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val maxOccupancy: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Double? = null,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val price: Double? = null,
    val currency: String? = null,
    val rules: String = "",
    val cancellationPolicy: String = "",
)
