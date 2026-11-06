// Unit tests for turning the register campsite form into a Campsite.
package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CampsiteFormTest {

    // Fill in the form with valid values, changing only what a test needs
    private fun fillForm(
        name: String = "Lake Louise Site 12",
        location: String = "Banff, AB",
        description: String = "Fire pit, picnic table",
        maxOccupancy: String = "6",
        latitude: String = "51.4254",
        longitude: String = "-116.1773",
        tags: String = "",
        imageUrl: String = "",
    ): Campsite? = campsiteFromForm(
        name, location, description, maxOccupancy, latitude, longitude, tags, imageUrl,
    )

    @Test
    fun campsiteFromForm_allValid_campsiteBuilt() {
        val campsite = fillForm()

        // verify every field was copied over
        assertNotNull(campsite)
        assertEquals("Lake Louise Site 12", campsite?.name)
        assertEquals("Banff, AB", campsite?.location)
        assertEquals("Fire pit, picnic table", campsite?.description)
        assertEquals(6, campsite?.maxOccupancy)
        assertEquals(51.4254, campsite?.latitude)
        assertEquals(-116.1773, campsite?.longitude)
    }

    @Test
    fun campsiteFromForm_blankName_null() {
        // verify a missing name is not allowed
        assertNull(fillForm(name = "   "))
    }

    @Test
    fun campsiteFromForm_occupancyZeroOrText_null() {
        // verify occupancy must be a whole number above 0
        assertNull(fillForm(maxOccupancy = "0"))
        assertNull(fillForm(maxOccupancy = "-2"))
        assertNull(fillForm(maxOccupancy = "six"))
        assertNull(fillForm(maxOccupancy = "4.5"))
    }

    @Test
    fun campsiteFromForm_latitudeOutOfRange_null() {
        // verify latitude must be a real place
        assertNull(fillForm(latitude = "91"))
        assertNull(fillForm(latitude = "-90.5"))
        assertNull(fillForm(latitude = "north"))
    }

    @Test
    fun campsiteFromForm_longitudeOutOfRange_null() {
        // verify longitude must be a real place
        assertNull(fillForm(longitude = "181"))
        assertNull(fillForm(longitude = "-180.5"))
    }

    @Test
    fun campsiteFromForm_tagsWithSpaces_trimmedAndBlanksDropped() {
        val campsite = fillForm(tags = " lake , hiking,, ")

        // verify tags are cleaned up
        assertEquals(listOf("lake", "hiking"), campsite?.tags)
    }

    @Test
    fun campsiteFromForm_blankImage_null() {
        // verify no image is saved as empty, but a real link is kept
        assertNull(fillForm(imageUrl = "  ")?.imageUrl)
        assertEquals("https://example.com/lake.jpg", fillForm(imageUrl = "https://example.com/lake.jpg")?.imageUrl)
    }

    @Test
    fun campsiteFromForm_extraSpaces_trimmed() {
        val campsite = fillForm(name = "  Lake Louise Site 12  ", location = " Banff, AB ")

        // verify spaces around the text are removed
        assertEquals("Lake Louise Site 12", campsite?.name)
        assertEquals("Banff, AB", campsite?.location)
    }
}
