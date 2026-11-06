// Intent tests for the admin opening campsite details and removed campsites disappearing from the app.
// AI use: Claude (Anthropic) added the open details and Back flow test.
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
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import com.example.code.model.ownedCampsites
import com.example.code.ui.entrant.CampsiteDetailsDestination
import com.example.code.ui.entrant.ExploreScreen
import com.example.code.ui.owner.OwnerCampsitesScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminCampsitesIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ownerId = "owner-device-7f3a"

    // Pretend Firestore: campsites still in the app
    private val campsites = mutableStateListOf<Campsite>()

    // Which screen is showing: "admin", "details", "explore" or "owner"
    private var screen by mutableStateOf("admin")

    // The campsite the admin opened on the details screen
    private var openedId = ""

    // Start on the admin screen before every single test
    @Before
    fun setUp() {
        campsites.clear()
        campsites.addAll(
            listOf(
                Campsite(
                    id = "site-lake-louise",
                    ownerId = ownerId,
                    name = "Lake Louise Site 12",
                    location = "Banff, AB",
                    description = "Fire pit, picnic table",
                    maxOccupancy = 6,
                ),
                Campsite(
                    id = "site-jasper",
                    ownerId = ownerId,
                    name = "Jasper Riverside",
                    location = "Jasper, AB",
                    maxOccupancy = 4,
                ),
            )
        )
        screen = "admin"
        openedId = ""

        composeTestRule.setContent {
            when (screen) {
                "admin" -> AdminCampsitesScreen(
                    campsites = campsites,
                    onCampsiteClick = { campsite ->
                        openedId = campsite.id
                        screen = "details"
                    },
                    onRemoveCampsite = { campsite, _ -> campsites.remove(campsite) },
                )

                "details" -> CampsiteDetailsDestination(
                    campsiteId = openedId,
                    campsites = campsites,
                    isLoading = false,
                    errorMessage = null,
                    onRetry = {},
                    onBack = { screen = "admin" },
                )

                "explore" -> ExploreScreen(
                    campsites = campsites,
                    onCampsiteClick = {},
                )

                else -> OwnerCampsitesScreen(
                    campsites = ownedCampsites(campsites, ownerId),
                    onRegisterCampsite = {},
                )
            }
        }
    }

    // Remove Lake Louise from the admin screen
    private fun removeLakeLouise() {
        composeTestRule.onNodeWithTag("removeButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveButton").performClick()
    }

    @Test
    fun openCampsite_adminTapsCampsite_detailsShownAndBack() {
        // the admin taps a campsite in the list
        composeTestRule.onNodeWithTag("campsiteCard-site-lake-louise").performClick()

        // verify its information is shown
        composeTestRule.onNodeWithText("Banff, AB").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fire pit, picnic table").performScrollTo().assertIsDisplayed()

        // go back to the admin list
        composeTestRule.onNodeWithText("Back").performScrollTo().performClick()

        // verify the admin list shows again
        composeTestRule.onNodeWithText("All campsites").assertIsDisplayed()
    }

    @Test
    fun removeCampsite_entrantExplores_removedCampsiteNotShown() {
        // the admin removes a campsite
        removeLakeLouise()

        // an entrant browses campsites afterward
        composeTestRule.runOnIdle { screen = "explore" }

        // verify the removed campsite is not listed, but the other one is
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertDoesNotExist()
        composeTestRule.onNodeWithText("Jasper Riverside").assertIsDisplayed()
    }

    @Test
    fun removeCampsite_ownerViewsList_removedCampsiteNotShown() {
        // the admin removes a campsite
        removeLakeLouise()

        // its owner opens My campsites afterward
        composeTestRule.runOnIdle { screen = "owner" }

        // verify it is no longer one of their campsites
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertDoesNotExist()
        composeTestRule.onNodeWithText("Jasper Riverside").assertIsDisplayed()
    }
}
