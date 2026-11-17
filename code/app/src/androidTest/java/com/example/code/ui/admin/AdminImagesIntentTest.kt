// Intent tests for the admin reviewing campsite images and the change showing elsewhere in the app.
package com.example.code.ui.admin

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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import com.example.code.ui.entrant.CampsiteDetailsDestination
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminImagesIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Pretend Firestore with the campsites saved so far
    private val campsites = mutableStateListOf<Campsite>()

    // Which screen is showing, either images, campsites or entrantDetails
    private var screen by mutableStateOf("images")

    // Start on the admin images tab with one campsite that has an image
    @Before
    fun setUp() {
        campsites.clear()
        campsites.add(
            Campsite(
                id = "site-lake-louise",
                ownerId = "owner-device-7f3a",
                name = "Lake Louise Site 12",
                location = "Banff, AB",
                description = "Fire pit, picnic table",
                maxOccupancy = 6,
                latitude = 51.4254,
                longitude = -116.1773,
                imageUrl = "https://example.com/lake.jpg",
            ),
        )
        screen = "images"

        composeTestRule.setContent {
            when (screen) {
                "images" -> AdminImagesScreen(
                    campsites = campsites,
                    onRemoveImage = { campsite, _ ->
                        // Remove the image like the app does
                        val index = campsites.indexOfFirst { it.id == campsite.id }
                        campsites[index] = campsite.copy(imageUrl = null)
                    },
                )

                "campsites" -> AdminCampsitesScreen(
                    campsites = campsites,
                    onRemoveCampsite = { campsite, _ -> campsites.remove(campsite) },
                )

                "entrantDetails" -> CampsiteDetailsDestination(
                    campsiteId = "site-lake-louise",
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
    fun browseImages_openAndBack_listShownAgain() {
        // open the image for review
        composeTestRule.onNodeWithTag("imageCard-site-lake-louise").performClick()
        composeTestRule.onNodeWithText("Banff, AB").performScrollTo().assertIsDisplayed()

        // go back
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify the list shows with the image still there
        composeTestRule.onNodeWithText("Uploaded images").assertIsDisplayed()
        composeTestRule.onNodeWithTag("imageCard-site-lake-louise").assertIsDisplayed()
    }

    @Test
    fun removeImage_confirmed_entrantSeesNoImage() {
        // admin removes the image
        composeTestRule.onNodeWithTag("imageCard-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("removeImageButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("confirmRemoveImageButton").performClick()

        // an entrant opens the campsite
        screen = "entrantDetails"

        // verify the campsite is still there but has no image
        composeTestRule.onNodeWithText("Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("No promotional image available").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun removeCampsite_byAdmin_imageGoneFromImages() {
        // admin removes the whole campsite from the campsites tab
        screen = "campsites"
        composeTestRule.onNodeWithTag("removeButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveButton").performClick()

        // go to the images tab
        screen = "images"

        // verify its image is no longer listed
        composeTestRule.onNodeWithText("No uploaded images yet.").assertIsDisplayed()
    }
}
