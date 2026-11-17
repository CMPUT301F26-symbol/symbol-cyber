// UI tests for editing a campsite with the register form filled in with its current details.
// AI use: Claude (Anthropic) wrote these tests from the edit campsite acceptance criteria, reviewed and run by Fahad.
package com.example.code.ui.owner

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditCampsiteScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // The campsite the owner is editing
    private val campsite = Campsite(
        id = "site-0",
        ownerId = "owner-device-7f3a",
        name = "Lake Louise Site 12",
        location = "Banff, AB",
        description = "Fire pit, picnic table",
        maxOccupancy = 6,
        latitude = 51.4254,
        longitude = -116.1773,
        tags = listOf("lake", "hiking"),
        imageUrl = "https://example.com/lake.jpg",
    )

    private var savedCampsite: Campsite? = null

    // How many times the form asked to save
    private var saveCount = 0

    // Set to true to pretend Firestore failed to save
    private var saveFails = false

    // Show a fresh edit screen before every single test
    @Before
    fun setUp() {
        savedCampsite = null
        saveCount = 0
        saveFails = false

        composeTestRule.setContent {
            RegisterCampsiteScreen(
                campsite = campsite,
                onRegister = { edited, showError ->
                    saveCount++
                    savedCampsite = edited
                    if (saveFails) {
                        showError("Could not save changes. Try again.")
                    }
                },
                onBack = {},
            )
        }
    }

    @Test
    fun editCampsite_opened_showsCurrentDetails() {
        // verify every field starts with the campsite's details
        composeTestRule.onNodeWithTag("nameInput").assertTextContains("Lake Louise Site 12")
        composeTestRule.onNodeWithTag("locationInput").assertTextContains("Banff, AB")
        composeTestRule.onNodeWithTag("descriptionInput").assertTextContains("Fire pit, picnic table")
        composeTestRule.onNodeWithTag("maxOccupancyInput").assertTextContains("6")
        composeTestRule.onNodeWithTag("latitudeInput").assertTextContains("51.4254")
        composeTestRule.onNodeWithTag("longitudeInput").assertTextContains("-116.1773")
        composeTestRule.onNodeWithTag("tagsInput").assertTextContains("lake, hiking")
        composeTestRule.onNodeWithTag("imageUrlInput").assertTextContains("https://example.com/lake.jpg")
    }

    @Test
    fun editCampsite_opened_showsEditTitleAndSaveButton() {
        // verify the form says it is editing, not registering
        composeTestRule.onNodeWithText("Edit campsite").assertExists()
        composeTestRule.onNodeWithText("Save changes").assertExists()
    }

    @Test
    fun editCampsite_nameChanged_newNameSaved() {
        // change only the name
        composeTestRule.onNodeWithTag("nameInput").performTextClearance()
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Lake Louise Site 14")

        // save the changes
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify the new name was sent and the other details stayed the same
        assertEquals("Lake Louise Site 14", savedCampsite?.name)
        assertEquals("Banff, AB", savedCampsite?.location)
        assertEquals(6, savedCampsite?.maxOccupancy)
    }

    @Test
    fun editCampsite_nameCleared_blocked() {
        // remove the name
        composeTestRule.onNodeWithTag("nameInput").performTextClearance()

        // try to save the changes
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify nothing was saved and the error shows
        assertNull(savedCampsite)
        composeTestRule
            .onNodeWithText("Please complete all required fields with valid values.")
            .assertExists()
    }

    @Test
    fun editCampsite_saveFails_canTryAgain() {
        // pretend Firestore is down
        saveFails = true

        // try to save the changes
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify the error shows and Save works again
        composeTestRule.onNodeWithText("Could not save changes. Try again.").assertExists()
        composeTestRule.onNodeWithTag("registerButton").assertIsEnabled()

        // try again
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify it was sent to be saved a second time
        assertEquals(2, saveCount)
    }
}
