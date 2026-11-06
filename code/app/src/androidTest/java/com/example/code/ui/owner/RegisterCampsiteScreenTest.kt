// UI tests for registering a campsite.
// AI use: Claude (Anthropic) added the tapped-twice and save-fails tests.
package com.example.code.ui.owner

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterCampsiteScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var savedCampsite: Campsite? = null

    // How many times the form asked to save
    private var saveCount = 0

    // Set to true to pretend Firestore failed to save
    private var saveFails = false

    // Show a fresh register screen before every single test
    @Before
    fun setUp() {
        savedCampsite = null
        saveCount = 0
        saveFails = false

        composeTestRule.setContent {
            RegisterCampsiteScreen(
                onRegister = { campsite, showError ->
                    saveCount++
                    savedCampsite = campsite
                    if (saveFails) {
                        showError("Could not save campsite. Try again.")
                    }
                },
                onBack = {},
            )
        }
    }

    // Fill in every required field with valid values
    private fun fillRequiredFields() {
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Lake Louise Site 12")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff, AB")
        composeTestRule.onNodeWithTag("descriptionInput").performTextInput("Fire pit, picnic table")
        composeTestRule.onNodeWithTag("maxOccupancyInput").performTextInput("6")
        composeTestRule.onNodeWithTag("latitudeInput").performTextInput("51.4254")
        composeTestRule.onNodeWithTag("longitudeInput").performTextInput("-116.1773")
    }

    @Test
    fun registerCampsite_allFieldsFilled_successful() {
        // fill in every required field
        fillRequiredFields()

        // register the campsite
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify the campsite was sent to be saved
        assertEquals("Lake Louise Site 12", savedCampsite?.name)
        assertEquals(6, savedCampsite?.maxOccupancy)
    }

    @Test
    fun registerCampsite_missingName_blocked() {
        // fill in everything except the name
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff, AB")
        composeTestRule.onNodeWithTag("descriptionInput").performTextInput("Fire pit, picnic table")
        composeTestRule.onNodeWithTag("maxOccupancyInput").performTextInput("6")
        composeTestRule.onNodeWithTag("latitudeInput").performTextInput("51.4254")
        composeTestRule.onNodeWithTag("longitudeInput").performTextInput("-116.1773")

        // try to register the campsite
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify nothing was saved
        assertNull(savedCampsite)

        // verify the error message shows
        composeTestRule
            .onNodeWithText("Please complete all required fields with valid values.")
            .assertExists()
    }

    @Test
    fun registerCampsite_allDetailsFilled_allDetailsSaved() {
        // fill in every field, including the optional ones
        fillRequiredFields()
        composeTestRule.onNodeWithTag("tagsInput").performTextInput("lake, hiking")
        composeTestRule.onNodeWithTag("imageUrlInput").performTextInput("https://example.com/lake.jpg")

        // register the campsite
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify every detail was sent to be saved
        assertEquals("Fire pit, picnic table", savedCampsite?.description)
        assertEquals(51.4254, savedCampsite?.latitude)
        assertEquals(-116.1773, savedCampsite?.longitude)
        assertEquals(listOf("lake", "hiking"), savedCampsite?.tags)
        assertEquals("https://example.com/lake.jpg", savedCampsite?.imageUrl)
    }

    @Test
    fun registerCampsite_imageAndTagsEmpty_savedWithoutThem() {
        // fill in only the required fields
        fillRequiredFields()

        // register the campsite
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify it still saved, with no image and no tags
        assertNotNull(savedCampsite)
        assertNull(savedCampsite?.imageUrl)
        assertEquals(emptyList<String>(), savedCampsite?.tags)
    }

    @Test
    fun registerCampsite_latitudeOutOfRange_blocked() {
        // fill in the fields, but with a latitude that isn't a real place
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Lake Louise Site 12")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff, AB")
        composeTestRule.onNodeWithTag("descriptionInput").performTextInput("Fire pit, picnic table")
        composeTestRule.onNodeWithTag("maxOccupancyInput").performTextInput("6")
        composeTestRule.onNodeWithTag("latitudeInput").performTextInput("200")
        composeTestRule.onNodeWithTag("longitudeInput").performTextInput("-116.1773")

        // try to register the campsite
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify nothing was saved
        assertNull(savedCampsite)
    }

    @Test
    fun registerCampsite_tappedTwice_savedOnce() {
        // fill in every required field
        fillRequiredFields()

        // tap Register twice while the first save is still going
        composeTestRule.onNodeWithTag("registerButton").performClick()
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify it was only sent to be saved once
        assertEquals(1, saveCount)
        composeTestRule.onNodeWithTag("registerButton").assertIsNotEnabled()
    }

    @Test
    fun registerCampsite_saveFails_canTryAgain() {
        // pretend Firestore is down
        saveFails = true
        fillRequiredFields()

        // try to register the campsite
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify the error shows and Register works again
        composeTestRule.onNodeWithText("Could not save campsite. Try again.").assertExists()
        composeTestRule.onNodeWithTag("registerButton").assertIsEnabled()

        // try again
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify it was sent to be saved a second time
        assertEquals(2, saveCount)
    }
}
