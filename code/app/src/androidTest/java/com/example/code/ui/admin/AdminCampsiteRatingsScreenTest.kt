// UI tests for the admin reviewing and removing the ratings of a campsite.
package com.example.code.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.model.Campsite
import com.example.code.model.Rating
import com.example.code.model.averageRating
import com.example.code.model.ratingsForCampsite
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminCampsiteRatingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fiveStars = Rating(
        id = "rating-1",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-lake-louise",
        score = 5,
    )

    private val oneStar = Rating(
        id = "rating-2",
        entrantId = "entrant-device-8e1f",
        campsiteId = "site-lake-louise",
        score = 1,
    )

    // A rating for another campsite, which should never show here
    private val watertonRating = Rating(
        id = "rating-3",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-waterton",
        score = 4,
    )

    // Pretend Firestore with the campsite and the ratings still in the app
    private var campsite by mutableStateOf<Campsite?>(null)
    private val ratings = mutableStateListOf<Rating>()

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    // Set to true to pretend Firestore failed to remove the rating
    private var removeFails = false

    private var retryTapped = false
    private var backTapped = false

    // Show a fresh ratings screen before every single test
    @Before
    fun setUp() {
        campsite = Campsite(
            id = "site-lake-louise",
            ownerId = "owner-device-7f3a",
            name = "Lake Louise Site 12",
            location = "Banff, AB",
            rating = 3.0,
        )
        ratings.clear()
        ratings.addAll(listOf(fiveStars, oneStar, watertonRating))
        isLoading = false
        errorMessage = null
        removeFails = false
        retryTapped = false
        backTapped = false

        composeTestRule.setContent {
            AdminCampsiteRatingsScreen(
                campsite = campsite,
                ratings = ratings,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { retryTapped = true },
                onBack = { backTapped = true },
                onRemoveRating = { rating, showError ->
                    // Remove it and update the overall rating like Firestore would, or show the error
                    if (removeFails) {
                        showError("Could not remove rating. Try again.")
                    } else {
                        ratings.remove(rating)
                        val remaining = ratingsForCampsite(ratings, rating.campsiteId)
                        campsite = campsite?.copy(rating = averageRating(remaining))
                    }
                },
            )
        }
    }

    @Test
    fun campsiteRatings_ratingsExist_onlyThisCampsiteListed() {
        // verify the overall rating and this campsite's ratings show
        composeTestRule.onNodeWithText("Ratings for Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overallRating").assertTextEquals("Rating: 3.0 / 5")
        composeTestRule.onNodeWithTag("ratingCard-rating-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ratingCard-rating-2").assertIsDisplayed()

        // verify the other campsite's rating is not listed
        composeTestRule.onNodeWithTag("ratingCard-rating-3").assertDoesNotExist()
    }

    @Test
    fun removeRating_confirmed_ratingGoneAndOverallUpdated() {
        // remove the one star rating
        composeTestRule.onNodeWithTag("removeRatingButton-rating-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveRatingButton").performClick()

        // verify it's gone and the overall rating only counts the five star rating
        composeTestRule.onNodeWithTag("ratingCard-rating-2").assertDoesNotExist()
        composeTestRule.onNodeWithTag("ratingCard-rating-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overallRating").assertTextEquals("Rating: 5.0 / 5")
    }

    @Test
    fun removeRating_lastRatingRemoved_noRatingsShown() {
        // remove both ratings
        composeTestRule.onNodeWithTag("removeRatingButton-rating-1").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveRatingButton").performClick()
        composeTestRule.onNodeWithTag("removeRatingButton-rating-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveRatingButton").performClick()

        // verify the campsite has no overall rating left
        composeTestRule.onNodeWithText("No ratings for this campsite yet.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overallRating").assertTextEquals("No ratings yet")
    }

    @Test
    fun removeRating_cancelled_ratingKept() {
        // tap Remove, then change your mind
        composeTestRule.onNodeWithTag("removeRatingButton-rating-2").performClick()
        composeTestRule.onNodeWithTag("cancelRemoveRatingButton").performClick()

        // verify the rating and overall rating did not change
        composeTestRule.onNodeWithTag("ratingCard-rating-2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("overallRating").assertTextEquals("Rating: 3.0 / 5")
    }

    @Test
    fun removeRating_firestoreFails_errorShown() {
        // Firestore will fail to remove
        removeFails = true

        composeTestRule.onNodeWithTag("removeRatingButton-rating-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveRatingButton").performClick()

        // verify the error shows and the rating is still listed
        composeTestRule.onNodeWithText("Could not remove rating. Try again.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ratingCard-rating-2").assertIsDisplayed()
    }

    @Test
    fun campsiteRatings_noRatings_emptyStateShown() {
        // nobody has rated this campsite
        ratings.removeAll(listOf(fiveStars, oneStar))

        // verify the empty message shows
        composeTestRule.onNodeWithText("No ratings for this campsite yet.").assertIsDisplayed()
    }

    @Test
    fun campsiteRatings_stillLoading_loadingShown() {
        // Firestore has not answered yet
        isLoading = true

        // verify loading shows instead of the ratings
        composeTestRule.onNodeWithText("Loading ratings…").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ratingCard-rating-1").assertDoesNotExist()
    }

    @Test
    fun campsiteRatings_loadFails_retryShown() {
        // Firestore failed to load
        errorMessage = "Failed to load ratings"

        // verify the error shows and Retry asks to load again
        composeTestRule.onNodeWithText("Unable to load ratings. Please try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryTapped)
    }

    @Test
    fun campsiteRatings_campsiteRemoved_notFoundShown() {
        // the campsite was removed while its ratings were open
        campsite = null

        // verify a not found message shows instead of ratings
        composeTestRule.onNodeWithText("Campsite not found. It may have been removed.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ratingCard-rating-1").assertDoesNotExist()
    }

    @Test
    fun campsiteRatings_backTapped_goesBack() {
        // tap Back
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify going back was asked for
        assertTrue(backTapped)
    }
}
