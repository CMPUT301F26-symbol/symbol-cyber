// Intent tests for the admin removing a campsite review and it no longer showing for that campsite.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
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
import com.example.code.model.Review
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminReviewsIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Pretend Firestore with the campsites and reviews saved so far
    private val campsites = mutableStateListOf<Campsite>()
    private val reviews = mutableStateListOf<Review>()

    // Which screen is showing, either campsites or reviews
    private var screen by mutableStateOf("campsites")

    // Start on the admin campsites tab with one campsite that has a good and a rude review
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
            ),
        )
        reviews.clear()
        reviews.addAll(
            listOf(
                Review(
                    id = "review-1",
                    entrantId = "entrant-device-2c9d",
                    campsiteId = "site-lake-louise",
                    text = "Great views of the lake and a quiet site.",
                ),
                Review(
                    id = "review-2",
                    entrantId = "entrant-device-8e1f",
                    campsiteId = "site-lake-louise",
                    text = "Rude review that breaks the app policy.",
                ),
            ),
        )
        screen = "campsites"

        composeTestRule.setContent {
            when (screen) {
                "campsites" -> AdminCampsitesScreen(
                    campsites = campsites,
                    onReviewsClick = { screen = "reviews" },
                    onRemoveCampsite = { campsite, _ -> campsites.remove(campsite) },
                )

                "reviews" -> AdminCampsiteReviewsScreen(
                    campsite = campsites.firstOrNull { it.id == "site-lake-louise" },
                    reviews = reviews,
                    onBack = { screen = "campsites" },
                    onRemoveReview = { review, _ -> reviews.remove(review) },
                )
            }
        }
    }

    @Test
    fun removeReview_confirmed_goneWhenCampsiteViewedAgain() {
        // admin removes the rude review
        composeTestRule.onNodeWithTag("reviewsButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("removeReviewButton-review-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveReviewButton").performClick()

        // go back, then view the campsite's reviews again
        composeTestRule.onNodeWithTag("backButton").performClick()
        composeTestRule.onNodeWithTag("reviewsButton-site-lake-louise").performClick()

        // verify the removed review is no longer shown but the good one still is
        composeTestRule.onNodeWithText("Rude review that breaks the app policy.").assertDoesNotExist()
        composeTestRule.onNodeWithText("Great views of the lake and a quiet site.").assertIsDisplayed()
    }

    @Test
    fun removeCampsite_byAdmin_reviewsNotFound() {
        // admin opens the reviews, but the campsite is removed meanwhile
        composeTestRule.onNodeWithTag("reviewsButton-site-lake-louise").performClick()
        campsites.clear()

        // verify the reviews screen says the campsite is gone
        composeTestRule.onNodeWithText("Campsite not found. It may have been removed.").assertIsDisplayed()
    }
}
