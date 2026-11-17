// Finds the campsites that have an image, so the admin can review the images.
package com.example.code.model

/**
 * Returns only the campsites that have an image link.
 *
 * @param campsites every campsite in the app
 * @return the campsites with a non-blank image link, in the same order
 */
fun campsitesWithImages(campsites: List<Campsite>): List<Campsite> {
    return campsites.filter { !it.imageUrl.isNullOrBlank() }
}
