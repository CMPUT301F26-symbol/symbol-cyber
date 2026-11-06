// Intent tests for an owner registering a campsite and seeing it listed.
package com.example.code.ui.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import com.example.code.model.ownedCampsites
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterCampsiteIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ownerId = "owner-device-7f3a"

    // Pretend Firestore: campsites saved so far
    private val campsites = mutableStateListOf<Campsite>()

    // Set to true to pretend Firestore failed
    private var saveFails = false

    // Which screen is showing: "list", "register" or the id of an opened campsite
    private var screen by mutableStateOf("list")

    // Start on My campsites before every single test
    @Before
    fun setUp() {
        campsites.clear()
        saveFails = false
        screen = "list"

        composeTestRule.setContent {
            when (screen) {
                "list" -> OwnerCampsitesScreen(
                    campsites = ownedCampsites(campsites, ownerId),
                    onRegisterCampsite = { screen = "register" },
                    onCampsiteClick = { screen = it.id },
                )

                "register" -> RegisterCampsiteScreen(
                    onRegister = { campsite, showError ->
                        // Save it like the app does, or show the error if saving fails
                        if (saveFails) {
                            showError("Could not save campsite. Try again.")
                        } else {
                            campsites.add(campsite.copy(id = "site-${campsites.size}", ownerId = ownerId))
                            screen = "list"
                        }
                    },
                    onBack = { screen = "list" },
                )

                else -> OwnerCampsiteDetailsDestination(
                    campsiteId = screen,
                    ownerId = ownerId,
                    campsites = campsites,
                    isLoading = false,
                    errorMessage = null,
                    onRetry = {},
                    onBack = { screen = "list" },
                )
            }
        }
    }

    // Open the form and fill in every required field
    private fun openFormAndFillRequiredFields() {
        composeTestRule.onNodeWithText("+ Register campsite").performClick()
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Lake Louise Site 12")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff, AB")
        composeTestRule.onNodeWithTag("descriptionInput").performTextInput("Fire pit, picnic table")
        composeTestRule.onNodeWithTag("maxOccupancyInput").performTextInput("6")
        composeTestRule.onNodeWithTag("latitudeInput").performTextInput("51.4254")
        composeTestRule.onNodeWithTag("longitudeInput").performTextInput("-116.1773")
    }

    @Test
    fun registerCampsite_validDetails_showsInMyCampsites() {
        // register a campsite
        openFormAndFillRequiredFields()
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify we are back on My campsites and it is listed
        composeTestRule.onNodeWithText("My campsites").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
    }

    @Test
    fun registerCampsite_missingName_staysOnForm() {
        // fill in everything except the name
        composeTestRule.onNodeWithText("+ Register campsite").performClick()
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff, AB")
        composeTestRule.onNodeWithTag("descriptionInput").performTextInput("Fire pit, picnic table")
        composeTestRule.onNodeWithTag("maxOccupancyInput").performTextInput("6")
        composeTestRule.onNodeWithTag("latitudeInput").performTextInput("51.4254")
        composeTestRule.onNodeWithTag("longitudeInput").performTextInput("-116.1773")
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify we stay on the form with the error showing
        composeTestRule.onNodeWithText("Please complete all required fields with valid values.").assertIsDisplayed()

        // verify nothing was added to My campsites
        composeTestRule.onNodeWithTag("backButton").performClick()
        composeTestRule.onNodeWithText("You have not registered any campsites yet.").assertIsDisplayed()
    }

    @Test
    fun registerCampsite_saveFails_showsErrorAndStaysOnForm() {
        // pretend Firestore is down
        saveFails = true

        // try to register a campsite
        openFormAndFillRequiredFields()
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // verify the error shows and we did not go back
        composeTestRule.onNodeWithText("Could not save campsite. Try again.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("registerButton").assertIsDisplayed()
    }

    @Test
    fun registerCampsite_allDetails_shownOnDetails() {
        // register a campsite with every detail, including the optional ones
        openFormAndFillRequiredFields()
        composeTestRule.onNodeWithTag("tagsInput").performTextInput("lake, hiking")
        composeTestRule.onNodeWithTag("imageUrlInput").performTextInput("https://example.com/lake.jpg")
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // open it from My campsites
        composeTestRule.onNodeWithText("Lake Louise Site 12").performClick()

        // verify every detail is shown
        composeTestRule.onNodeWithText("Fire pit, picnic table").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("51.4254, -116.1773").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("lake, hiking").performScrollTo().assertIsDisplayed()

        // verify the picture is shown
        composeTestRule.onNodeWithContentDescription("Promotional image for Lake Louise Site 12").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun registerCampsite_noImageOrTags_savedAndShown() {
        // register a campsite with only the required fields
        openFormAndFillRequiredFields()
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // open it from My campsites
        composeTestRule.onNodeWithText("Lake Louise Site 12").performClick()

        // verify it saved without the optional fields
        composeTestRule.onNodeWithText("No tags").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("No promotional image available").performScrollTo().assertIsDisplayed()
    }
}
