// UI tests for the admin browsing, reviewing and removing campsite images.
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminImagesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val lakeLouise = Campsite(
        id = "site-lake-louise",
        ownerId = "owner-device-7f3a",
        name = "Lake Louise Site 12",
        location = "Banff, AB",
        imageUrl = "https://example.com/lake.jpg",
    )

    private val jasper = Campsite(
        id = "site-jasper",
        ownerId = "other-owner-device-91bc",
        name = "Jasper Whistlers 4",
        location = "Jasper, AB",
        imageUrl = null,
    )

    // Pretend Firestore with the campsites still in the app
    private val campsites = mutableStateListOf<Campsite>()

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    // Set to true to pretend Firestore failed to remove the image
    private var removeFails = false

    private var retryTapped = false

    // Show a fresh images screen before every single test
    @Before
    fun setUp() {
        campsites.clear()
        campsites.addAll(listOf(lakeLouise, jasper))
        isLoading = false
        errorMessage = null
        removeFails = false
        retryTapped = false

        composeTestRule.setContent {
            AdminImagesScreen(
                campsites = campsites,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { retryTapped = true },
                onRemoveImage = { campsite, showError ->
                    // Remove the image like Firestore would, or show the error if removing fails
                    if (removeFails) {
                        showError("Could not remove image. Try again.")
                    } else {
                        val index = campsites.indexOfFirst { it.id == campsite.id }
                        campsites[index] = campsite.copy(imageUrl = null)
                    }
                },
            )
        }
    }

    // Open the Lake Louise image for review
    private fun openLakeLouiseImage() {
        composeTestRule.onNodeWithTag("imageCard-site-lake-louise").performClick()
    }

    @Test
    fun browseImages_campsiteWithImage_shown() {
        // verify the image and its campsite name are shown
        composeTestRule.onNodeWithText("Image for Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Uploaded image for Lake Louise Site 12").assertIsDisplayed()

        // verify a campsite without an image is not listed
        composeTestRule.onNodeWithText("Image for Jasper Whistlers 4").assertDoesNotExist()
    }

    @Test
    fun browseImages_noImages_emptyMessageShown() {
        // remove every campsite that has an image
        campsites.remove(lakeLouise)

        // verify the empty message shows
        composeTestRule.onNodeWithText("No uploaded images yet.").assertIsDisplayed()
    }

    @Test
    fun browseImages_loadingAndError_statesShown() {
        // pretend Firestore is still loading
        isLoading = true
        composeTestRule.onNodeWithText("Loading images…").assertIsDisplayed()

        // pretend Firestore failed
        isLoading = false
        errorMessage = "Failed to load campsites"
        composeTestRule.onNodeWithText("Unable to load images. Please try again.").assertIsDisplayed()

        // verify Retry asks to load again
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryTapped)
    }

    @Test
    fun reviewImage_tapped_imageAndDetailsShown() {
        // open the image
        openLakeLouiseImage()

        // verify the image, its campsite and the remove action are shown
        composeTestRule.onNodeWithContentDescription("Uploaded image for Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Banff, AB").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("https://example.com/lake.jpg").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("removeImageButton").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun reviewImage_backTapped_listShown() {
        // open the image, then go back
        openLakeLouiseImage()
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify the list is showing again
        composeTestRule.onNodeWithText("Uploaded images").assertIsDisplayed()
    }

    @Test
    fun removeImage_confirmed_imageGoneFromList() {
        // open the image and remove it
        openLakeLouiseImage()
        composeTestRule.onNodeWithTag("removeImageButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("confirmRemoveImageButton").performClick()

        // verify the image is gone but the campsite is still in the app
        composeTestRule.onNodeWithText("No uploaded images yet.").assertIsDisplayed()
        assertTrue(campsites.any { it.id == "site-lake-louise" })
    }

    @Test
    fun removeImage_cancelled_imageKept() {
        // open the image, tap Remove, then cancel
        openLakeLouiseImage()
        composeTestRule.onNodeWithTag("removeImageButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("cancelRemoveImageButton").performClick()

        // verify the image is still there
        composeTestRule.onNodeWithText("Image for Lake Louise Site 12").assertIsDisplayed()
        assertTrue(campsites.first { it.id == "site-lake-louise" }.imageUrl != null)
    }

    @Test
    fun removeImage_removeFails_errorShown() {
        // pretend Firestore is down
        removeFails = true

        // try to remove the image
        openLakeLouiseImage()
        composeTestRule.onNodeWithTag("removeImageButton").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("confirmRemoveImageButton").performClick()

        // verify the error shows and the image is still listed
        composeTestRule.onNodeWithText("Could not remove image. Try again.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("imageCard-site-lake-louise").assertIsDisplayed()
    }
}
