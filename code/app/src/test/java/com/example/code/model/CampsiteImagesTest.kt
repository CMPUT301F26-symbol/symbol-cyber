// Unit tests for finding the campsites that have an image.
package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CampsiteImagesTest {

    private val lakeLouise = Campsite(
        id = "site-lake-louise",
        name = "Lake Louise Site 12",
        imageUrl = "https://example.com/lake.jpg",
    )

    private val waterton = Campsite(
        id = "site-waterton",
        name = "Waterton Lakeshore",
        imageUrl = "https://example.com/waterton.jpg",
    )

    private val noImage = Campsite(id = "site-jasper", name = "Jasper Whistlers 4", imageUrl = null)

    private val blankImage = Campsite(id = "site-kananaskis", name = "Kananaskis Boulton 9", imageUrl = "  ")

    @Test
    fun campsitesWithImages_mixedCampsites_onlyImagesKept() {
        val result = campsitesWithImages(listOf(lakeLouise, noImage, waterton, blankImage))

        // verify only campsites with a real image link are kept, in the same order
        assertEquals(listOf(lakeLouise, waterton), result)
    }

    @Test
    fun campsitesWithImages_noImages_emptyList() {
        // verify campsites without images give an empty list
        assertEquals(emptyList<Campsite>(), campsitesWithImages(listOf(noImage, blankImage)))
    }

    @Test
    fun campsitesWithImages_noCampsites_emptyList() {
        // verify an empty app gives an empty list
        assertEquals(emptyList<Campsite>(), campsitesWithImages(emptyList()))
    }
}
