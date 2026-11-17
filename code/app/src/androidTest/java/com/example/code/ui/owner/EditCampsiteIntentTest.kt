// Intent tests for an owner editing their campsite, and other users not being able to.
// AI use: Claude (Anthropic) wrote these tests from the edit campsite acceptance criteria, reviewed and run by Fahad.
package com.example.code.ui.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import com.example.code.model.ownedCampsites
import com.example.code.model.updateFromForm
import com.example.code.ui.entrant.CampsiteDetailsDestination
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditCampsiteIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ownerId = "owner-device-7f3a"

    // Pretend Firestore with the campsites saved so far
    private val campsites = mutableStateListOf<Campsite>()

    // Who is looking at the owner details screen
    private var viewerId by mutableStateOf(ownerId)

    // Which screen is showing, either list, details, edit or entrantDetails
    private var screen by mutableStateOf("list")

    // Start on My campsites with one saved campsite before every single test
    @Before
    fun setUp() {
        campsites.clear()
        campsites.add(
            Campsite(
                id = "site-0",
                ownerId = ownerId,
                name = "Lake Louise Site 12",
                location = "Banff, AB",
                description = "Fire pit, picnic table",
                maxOccupancy = 6,
                latitude = 51.4254,
                longitude = -116.1773,
            ),
        )
        viewerId = ownerId
        screen = "list"

        composeTestRule.setContent {
            when (screen) {
                "list" -> OwnerCampsitesScreen(
                    campsites = ownedCampsites(campsites, ownerId),
                    onRegisterCampsite = {},
                    onCampsiteClick = { screen = "details" },
                )

                "details" -> OwnerCampsiteDetailsDestination(
                    campsiteId = "site-0",
                    ownerId = viewerId,
                    campsites = campsites,
                    isLoading = false,
                    errorMessage = null,
                    onRetry = {},
                    onBack = { screen = "list" },
                    onEditCampsite = { screen = "edit" },
                )

                "edit" -> RegisterCampsiteScreen(
                    campsite = campsites[0],
                    onRegister = { edited, _ ->
                        // Save it like the app does, then go back to the details
                        campsites[0] = updateFromForm(campsites[0], edited)
                        screen = "details"
                    },
                    onBack = { screen = "details" },
                )

                "entrantDetails" -> CampsiteDetailsDestination(
                    campsiteId = "site-0",
                    campsites = campsites,
                    isLoading = false,
                    errorMessage = null,
                    onRetry = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun editCampsite_nameChanged_shownOnDetailsAndList() {
        // open the campsite and tap Edit
        composeTestRule.onNodeWithText("Lake Louise Site 12").performClick()
        composeTestRule.onNodeWithTag("editCampsiteButton").performScrollTo().performClick()

        // change the name and save
        composeTestRule.onNodeWithTag("nameInput").performTextClearance()
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Lake Louise Site 14")
        composeTestRule.onNodeWithTag("registerButton").performScrollTo().performClick()

        // verify the details show the new name
        composeTestRule.onNodeWithText("Lake Louise Site 14").assertIsDisplayed()

        // verify My campsites shows the new name too
        screen = "list"
        composeTestRule.onNodeWithText("Lake Louise Site 14").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertDoesNotExist()
    }

    @Test
    fun editCampsite_backPressed_nothingChanged() {
        // open the edit form and change the name
        composeTestRule.onNodeWithText("Lake Louise Site 12").performClick()
        composeTestRule.onNodeWithTag("editCampsiteButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("nameInput").performTextClearance()
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Lake Louise Site 14")

        // go back without saving
        composeTestRule.onNodeWithTag("backButton").performScrollTo().performClick()

        // verify the old name is still shown
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lake Louise Site 14").assertDoesNotExist()
    }

    @Test
    fun editCampsite_notOwner_noEditButton() {
        // another owner opens the campsite
        viewerId = "owner-device-9b21"
        screen = "details"

        // verify they can't see it or edit it
        composeTestRule.onNodeWithText("Campsite not found. It may have been removed.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("editCampsiteButton").assertDoesNotExist()
    }

    @Test
    fun editCampsite_entrantViewsCampsite_noEditButton() {
        // an entrant opens the campsite
        screen = "entrantDetails"

        // verify the campsite shows, but with no Edit button
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithTag("editCampsiteButton").assertDoesNotExist()
        composeTestRule.onNodeWithText("Edit campsite").assertDoesNotExist()
    }
}
