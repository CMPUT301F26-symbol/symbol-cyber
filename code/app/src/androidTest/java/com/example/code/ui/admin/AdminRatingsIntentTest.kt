// Intent tests for the admin removing a campsite rating and the new overall rating showing to entrants.
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
import com.example.code.model.Rating
import com.example.code.model.averageRating
import com.example.code.model.ratingsForCampsite
import com.example.code.ui.entrant.CampsiteDetailsDestination
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminRatingsIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Pretend Firestore with the campsites and ratings saved so far
    private val campsites = mutableStateListOf<Campsite>()
    private val ratings = mutableStateListOf<Rating>()

    // Which screen is showing, either campsites, ratings or entrantDetails
    private var screen by mutableStateOf("campsites")

    // Start on the admin campsites tab with one campsite rated 5 and 1
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
                rating = 3.0,
            ),
        )
        ratings.clear()
        ratings.addAll(
            listOf(
                Rating(id = "rating-1", entrantId = "entrant-device-2c9d", campsiteId = "site-lake-louise", score = 5),
                Rating(id = "rating-2", entrantId = "entrant-device-8e1f", campsiteId = "site-lake-louise", score = 1),
            ),
        )
        screen = "campsites"

        composeTestRule.setContent {
            when (screen) {
                "campsites" -> AdminCampsitesScreen(
                    campsites = campsites,
                    onRatingsClick = { screen = "ratings" },
                )

                "ratings" -> AdminCampsiteRatingsScreen(
                    campsite = campsites.firstOrNull { it.id == "site-lake-louise" },
                    ratings = ratings,
                    onBack = { screen = "campsites" },
                    onRemoveRating = { rating, _ ->
                        // Remove it and update the overall rating like the app does
                        ratings.remove(rating)
                        val remaining = ratingsForCampsite(ratings, rating.campsiteId)
                        val index = campsites.indexOfFirst { it.id == rating.campsiteId }
                        campsites[index] = campsites[index].copy(rating = averageRating(remaining))
                    },
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
    fun browseRatings_openAndBack_campsitesShownAgain() {
        // open the campsite's ratings
        composeTestRule.onNodeWithTag("ratingsButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("ratingCard-rating-2").assertIsDisplayed()

        // go back
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify the campsites list shows again
        composeTestRule.onNodeWithText("All campsites").assertIsDisplayed()
    }

    @Test
    fun removeRating_confirmed_entrantSeesNewOverallRating() {
        // admin removes the one star rating
        composeTestRule.onNodeWithTag("ratingsButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("removeRatingButton-rating-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveRatingButton").performClick()

        // an entrant opens the campsite
        screen = "entrantDetails"

        // verify the overall rating no longer counts the removed rating
        composeTestRule.onNodeWithText("Rating: 5.0 / 5").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun removeRating_allRemoved_entrantSeesNoRatings() {
        // admin removes both ratings
        composeTestRule.onNodeWithTag("ratingsButton-site-lake-louise").performClick()
        composeTestRule.onNodeWithTag("removeRatingButton-rating-1").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveRatingButton").performClick()
        composeTestRule.onNodeWithTag("removeRatingButton-rating-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveRatingButton").performClick()

        // an entrant opens the campsite
        screen = "entrantDetails"

        // verify the campsite shows it has no ratings
        composeTestRule.onNodeWithText("No ratings yet").performScrollTo().assertIsDisplayed()
    }
}
