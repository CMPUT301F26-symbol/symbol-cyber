// UI tests for the admin removing campsites.
package com.example.code.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminCampsitesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val lakeLouise = Campsite(
        id = "site-lake-louise",
        ownerId = "owner-device-7f3a",
        name = "Lake Louise Site 12",
        location = "Banff, AB",
        maxOccupancy = 6,
    )

    private val waterton = Campsite(
        id = "site-waterton",
        ownerId = "other-owner-device-91bc",
        name = "Waterton Lakeshore",
        location = "Waterton, AB",
        maxOccupancy = 8,
    )

    // Pretend Firestore: campsites still in the app
    private val campsites = mutableStateListOf<Campsite>()

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    // Set to true to pretend Firestore failed to remove
    private var removeFails = false

    private var retryTapped = false

    // The campsite the admin tapped to open
    private var openedCampsite: Campsite? = null

    // Show a fresh admin screen before every single test
    @Before
    fun setUp() {
        campsites.clear()
        campsites.addAll(listOf(lakeLouise, waterton))
        isLoading = false
        errorMessage = null
        removeFails = false
        retryTapped = false
        openedCampsite = null

        composeTestRule.setContent {
            AdminCampsitesScreen(
                campsites = campsites,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { retryTapped = true },
                onCampsiteClick = { openedCampsite = it },
                onRemoveCampsite = { campsite, showError ->
                    // Remove it like Firestore would, or show the error if removing fails
                    if (removeFails) {
                        showError("Could not remove campsite. Try again.")
                    } else {
                        campsites.remove(campsite)
                    }
                },
            )
        }
    }

    @Test
    fun adminCampsites_campsitesExist_allListed() {
        // verify campsites from every owner are listed
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Waterton Lakeshore").assertIsDisplayed()
    }

    @Test
    fun openCampsite_cardTapped_campsiteOpened() {
        // tap the Waterton campsite
        composeTestRule.onNodeWithTag("campsiteCard-site-waterton").performClick()

        // verify that exact campsite is opened
        assertEquals(waterton, openedCampsite)
    }

    @Test
    fun removeCampsite_confirmed_campsiteRemoved() {
        // tap Remove on Lake Louise
        composeTestRule.onNodeWithTag("removeButton-site-lake-louise").performClick()

        // verify the admin is asked to confirm
        composeTestRule.onNodeWithText("Remove Lake Louise Site 12?").assertIsDisplayed()

        // verify tapping Remove did not open the campsite
        assertEquals(null, openedCampsite)

        // confirm the removal
        composeTestRule.onNodeWithTag("confirmRemoveButton").performClick()

        // verify it is gone and the other campsite is still there
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertDoesNotExist()
        composeTestRule.onNodeWithText("Waterton Lakeshore").assertIsDisplayed()
        assertEquals(listOf(waterton), campsites.toList())
    }

    @Test
    fun removeCampsite_cancelled_campsiteKept() {
        // tap Remove, then change our mind
        composeTestRule.onNodeWithTag("removeButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("cancelRemoveButton").performClick()

        // verify nothing was removed
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
        assertEquals(2, campsites.size)
    }

    @Test
    fun removeCampsite_removeFails_errorShownAndCampsiteKept() {
        // pretend Firestore is down
        removeFails = true

        // try to remove Lake Louise
        composeTestRule.onNodeWithTag("removeButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveButton").performClick()

        // verify the error shows and the campsite is still listed
        composeTestRule.onNodeWithText("Could not remove campsite. Try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
    }

    @Test
    fun adminCampsites_noCampsites_emptyStateShown() {
        // every campsite has been removed
        campsites.clear()

        // verify the empty message shows
        composeTestRule.onNodeWithText("No campsites to review yet.").assertIsDisplayed()
    }

    @Test
    fun adminCampsites_stillLoading_loadingShown() {
        // Firestore has not answered yet
        isLoading = true

        // verify loading shows instead of the list
        composeTestRule.onNodeWithText("Loading campsites…").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertDoesNotExist()
    }

    @Test
    fun adminCampsites_loadFails_retryShown() {
        // Firestore failed to load
        errorMessage = "Failed to load campsites"

        // verify the error shows and Retry asks to load again
        composeTestRule.onNodeWithText("Unable to load campsites. Please try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryTapped)
    }
}
