// Checks what an owner typed in the register campsite form and turns it into a Campsite.
package com.example.code.model

/**
 * Builds a campsite from the register form, or returns null if something is missing or invalid.
 *
 * @param name the campsite name, required
 * @param location where the campsite is, required
 * @param description the description and amenities, required
 * @param maxOccupancy the maximum number of guests, must be a whole number above 0
 * @param latitude the map latitude, must be between -90 and 90
 * @param longitude the map longitude, must be between -180 and 180
 * @param tags comma-separated tags, optional
 * @param imageUrl a link to a promotional image, optional
 * @return the campsite to save, or null if the form is not valid
 */
fun campsiteFromForm(
    name: String,
    location: String,
    description: String,
    maxOccupancy: String,
    latitude: String,
    longitude: String,
    tags: String,
    imageUrl: String,
): Campsite? {
    val occupancyValue = maxOccupancy.toIntOrNull()
    val latitudeValue = latitude.toDoubleOrNull()
    val longitudeValue = longitude.toDoubleOrNull()

    if (
        name.isBlank() ||
        location.isBlank() ||
        description.isBlank() ||
        occupancyValue == null ||
        occupancyValue <= 0 ||
        latitudeValue == null ||
        longitudeValue == null ||
        latitudeValue !in -90.0..90.0 || // latitude must be a real place
        longitudeValue !in -180.0..180.0 // longitude must be a real place
    ) {
        return null
    }

    return Campsite(
        name = name.trim(),
        location = location.trim(),
        description = description.trim(),
        maxOccupancy = occupancyValue,
        latitude = latitudeValue,
        longitude = longitudeValue,
        tags = tags
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() },
        // Leave it empty if no image was given
        imageUrl = imageUrl.trim().ifBlank { null },
    )
}
