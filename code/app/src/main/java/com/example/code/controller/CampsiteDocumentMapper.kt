package com.example.code.controller

import com.example.code.model.Campsite
import com.google.firebase.firestore.GeoPoint

/** Reads current records and the legacy Firestore field names used by the team. */
internal fun campsiteFromDocument(id: String, data: Map<String, Any?>): Campsite {
    val location = data["location"]
    return Campsite(
        id = id,
        ownerId = data["ownerId"] as? String ?: "",
        name = data["name"] as? String ?: "",
        location = when (location) {
            is String -> location
            is GeoPoint -> "${location.latitude}, ${location.longitude}"
            else -> ""
        },
        description = data["description"] as? String ?: data["amenities"] as? String ?: "",
        maxOccupancy = (data["maxOccupancy"] as? Number)?.toInt()
            ?: (data["maximumOccupancy"] as? Number)?.toInt() ?: 0,
        latitude = (data["latitude"] as? Number)?.toDouble() ?: (location as? GeoPoint)?.latitude ?: 0.0,
        longitude = (data["longitude"] as? Number)?.toDouble() ?: (location as? GeoPoint)?.longitude ?: 0.0,
        rating = (data["rating"] as? Number)?.toDouble() ?: (data["averageRating"] as? Number)?.toDouble(),
        imageUrl = data["imageUrl"] as? String,
        tags = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        price = (data["price"] as? Number)?.toDouble() ?: (data["pricePerNight"] as? Number)?.toDouble(),
        currency = data["currency"] as? String,
        rules = data["rules"] as? String ?: "",
        cancellationPolicy = data["cancellationPolicy"] as? String ?: "",
    )
}

// The following block(s) of code were generated primarily by GPt-6 Sol, "Please implement US 02.13.01. Make sure to look through existing code and add all UI elements, logic, and test cases", November 27, 2026.
/** Owner listing edits never replace scores or aggregate fields maintained by rating transactions. */
internal fun campsiteOwnerUpdateToMap(campsite: Campsite): Map<String, Any?> = mapOf(
    "name" to campsite.name,
    "location" to campsite.location,
    "description" to campsite.description,
    "maxOccupancy" to campsite.maxOccupancy,
    "latitude" to campsite.latitude,
    "longitude" to campsite.longitude,
    "imageUrl" to campsite.imageUrl,
    "tags" to campsite.tags,
    "price" to campsite.price,
    "currency" to campsite.currency,
    "rules" to campsite.rules,
    "cancellationPolicy" to campsite.cancellationPolicy,
)

