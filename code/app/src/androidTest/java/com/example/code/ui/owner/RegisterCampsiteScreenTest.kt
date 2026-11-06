// UI tests for registering a campsite.
package com.example.code.ui.owner

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class RegisterCampsiteScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var savedCampsite: Campsite? = null

    // Show a fresh register screen before every single test
    @Before
    fun setUp() {
        savedCampsite = null

        composeTestRule.setContent {
            RegisterCampsiteScreen(
                onRegister = { campsite, _ -> savedCampsite = campsite },
                onBack = {},
            )
        }
    }

    @Test
    fun registerCampsite_allFieldsFilled_successful() {
        // fill in every required field
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Lake Louise Site 12")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff, AB")
        composeTestRule.onNodeWithTag("descriptionInput").performTextInput("Fire pit, picnic table")
        composeTestRule.onNodeWithTag("maxOccupancyInput").performTextInput("6")
        composeTestRule.onNodeWithTag("latitudeInput").performTextInput("51.4254")
        composeTestRule.onNodeWithTag("longitudeInput").performTextInput("-116.1773")

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
}
