// UI tests for the owner's list of campsites.
package com.example.code.ui.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerCampsitesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val lakeLouise = Campsite(
        id = "site-lake-louise",
        ownerId = "owner-device-7f3a",
        name = "Lake Louise Site 12",
        location = "Banff, AB",
        maxOccupancy = 6,
    )

    private val jasper = Campsite(
        id = "site-jasper",
        ownerId = "owner-device-7f3a",
        name = "Jasper Riverside",
        location = "Jasper, AB",
        maxOccupancy = 4,
    )

    // What the screen is showing, changed by each test
    private var campsites by mutableStateOf(emptyList<Campsite>())
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    private var retryTapped = false
    private var registerTapped = false

    // Show a fresh list screen before every single test
    @Before
    fun setUp() {
        campsites = emptyList()
        isLoading = false
        errorMessage = null
        retryTapped = false
        registerTapped = false

        composeTestRule.setContent {
            OwnerCampsitesScreen(
                campsites = campsites,
                onRegisterCampsite = { registerTapped = true },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { retryTapped = true },
            )
        }
    }

    @Test
    fun ownerCampsites_registeredCampsites_displayed() {
        // the owner has two campsites
        campsites = listOf(lakeLouise, jasper)

        // verify both are listed with their details
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banff, AB").assertIsDisplayed()
        composeTestRule.onNodeWithText("Maximum occupancy: 6").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jasper Riverside").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jasper, AB").assertIsDisplayed()

        // verify the empty message is not shown
        composeTestRule.onNodeWithText("You have not registered any campsites yet.").assertDoesNotExist()
    }

    @Test
    fun ownerCampsites_noCampsites_emptyStateShown() {
        // the owner has no campsites (the default)

        // verify the empty message shows
        composeTestRule.onNodeWithText("You have not registered any campsites yet.").assertIsDisplayed()
    }

    @Test
    fun ownerCampsites_stillLoading_loadingShown() {
        // Firestore has not answered yet
        isLoading = true

        // verify loading shows instead of the empty message
        composeTestRule.onNodeWithText("Loading your campsites…").assertIsDisplayed()
        composeTestRule.onNodeWithText("You have not registered any campsites yet.").assertDoesNotExist()
    }

    @Test
    fun ownerCampsites_loadFails_retryShown() {
        // Firestore failed to load
        errorMessage = "Failed to load campsites"

        // verify the error shows instead of the empty message
        composeTestRule.onNodeWithText("Unable to load your campsites. Please try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("You have not registered any campsites yet.").assertDoesNotExist()

        // verify Retry asks to load again
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryTapped)
    }

    @Test
    fun ownerCampsites_registerTapped_opensForm() {
        // tap the register button
        composeTestRule.onNodeWithText("+ Register campsite").performClick()

        // verify the form was asked to open
        assertTrue(registerTapped)
    }
}
