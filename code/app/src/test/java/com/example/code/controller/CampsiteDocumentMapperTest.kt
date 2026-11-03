package com.example.code.controller

import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.*
import org.junit.Test

class CampsiteDocumentMapperTest {
    @Test fun readsFullStoredDetailsUsingDocumentIdentity() {
        val campsite = campsiteFromDocument("real-id", mapOf(
            "id" to "stale-id", "ownerId" to "owner", "name" to "Pine", "location" to "Banff",
            "description" to "Water and firepit", "maxOccupancy" to 4L,
            "latitude" to 51.1, "longitude" to -115.5, "rating" to 4.5,
            "imageUrl" to "https://example.com/pine.jpg", "tags" to listOf("Lake", 3),
            "price" to 35L, "currency" to "CAD", "rules" to "Quiet after 10",
            "cancellationPolicy" to "Cancel 48 hours ahead",
        ))
        assertEquals("real-id", campsite.id)
        assertEquals("owner", campsite.ownerId)
        assertEquals("Pine", campsite.name)
        assertEquals("Banff", campsite.location)
        assertEquals("Water and firepit", campsite.description)
        assertEquals(4, campsite.maxOccupancy)
        assertEquals(51.1, campsite.latitude, 0.0)
        assertEquals(-115.5, campsite.longitude, 0.0)
        assertEquals(4.5, campsite.rating)
        assertEquals("https://example.com/pine.jpg", campsite.imageUrl)
        assertEquals(listOf("Lake"), campsite.tags)
        assertEquals(35.0, campsite.price)
        assertEquals("CAD", campsite.currency)
        assertEquals("Quiet after 10", campsite.rules)
        assertEquals("Cancel 48 hours ahead", campsite.cancellationPolicy)
    }

    @Test fun legacyDocumentsRemainReadableWithoutInventingOptionalDetails() {
        val campsite = campsiteFromDocument("legacy", mapOf(
            "location" to GeoPoint(51.0, -115.0), "amenities" to "Firepit",
            "maximumOccupancy" to 6L, "averageRating" to 4L, "pricePerNight" to 20L,
        ))
        assertEquals("51.0, -115.0", campsite.location)
        assertEquals(51.0, campsite.latitude, 0.0)
        assertEquals(-115.0, campsite.longitude, 0.0)
        assertEquals("Firepit", campsite.description)
        assertEquals(6, campsite.maxOccupancy)
        assertEquals(4.0, campsite.rating)
        assertEquals(20.0, campsite.price)
        assertNull(campsite.imageUrl)
        assertNull(campsite.currency)
        assertEquals("", campsite.rules)
        assertEquals("", campsite.cancellationPolicy)
        assertNull(campsiteFromDocument("old", emptyMap()).price)
    }
}
