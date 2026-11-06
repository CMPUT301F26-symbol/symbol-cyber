// Intent tests for an owner viewing only their own campsites.
package com.example.code.ui.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import com.example.code.model.ownedCampsites
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnerCampsitesIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ownerId = "owner-device-7f3a"

    // Pretend Firestore: campsites from two different owners
    private val allCampsites = listOf(
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
        Campsite(
            id = "site-someone-else",
            ownerId = "other-owner-device-91bc",
            name = "Waterton Lakeshore",
            location = "Waterton, AB",
            maxOccupancy = 8,
        ),
    )

    // Which screen is showing: "list" or the id of an opened campsite
    private var screen by mutableStateOf("list")

    // Start on My campsites before every single test
    @Before
    fun setUp() {
        screen = "list"

        composeTestRule.setContent {
            if (screen == "list") {
                OwnerCampsitesScreen(
                    campsites = ownedCampsites(allCampsites, ownerId),
                    onRegisterCampsite = {},
                    onCampsiteClick = { screen = it.id },
                )
            } else {
                OwnerCampsiteDetailsDestination(
                    campsiteId = screen,
                    ownerId = ownerId,
                    campsites = allCampsites,
                    isLoading = false,
                    errorMessage = null,
                    onRetry = {},
                    onBack = { screen = "list" },
                )
            }
        }
    }

    @Test
    fun ownerCampsites_mixedOwners_onlyMineShown() {
        // verify both of my campsites are listed
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jasper Riverside").assertIsDisplayed()

        // verify the other owner's campsite is not
        composeTestRule.onNodeWithText("Waterton Lakeshore").assertDoesNotExist()
    }

    @Test
    fun ownerCampsites_openCampsite_detailsShownAndBack() {
        // open one of my campsites
        composeTestRule.onNodeWithText("Lake Louise Site 12").performClick()

        // verify its details show
        composeTestRule.onNodeWithText("Fire pit, picnic table").performScrollTo().assertIsDisplayed()

        // go back to the list
        composeTestRule.onNodeWithText("Back").performScrollTo().performClick()

        // verify we are back on My campsites
        composeTestRule.onNodeWithText("My campsites").assertIsDisplayed()
    }
}
