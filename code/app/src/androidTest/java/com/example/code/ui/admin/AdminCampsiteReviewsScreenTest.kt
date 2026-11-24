// UI tests for the admin reviewing and removing the reviews of a campsite.
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminCampsiteReviewsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val goodReview = Review(
        id = "review-1",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-lake-louise",
        text = "Great views of the lake and a quiet site.",
    )

    private val rudeReview = Review(
        id = "review-2",
        entrantId = "entrant-device-8e1f",
        campsiteId = "site-lake-louise",
        text = "Rude review that breaks the app policy.",
    )

    // A review for another campsite, which should never show here
    private val watertonReview = Review(
        id = "review-3",
        entrantId = "entrant-device-2c9d",
        campsiteId = "site-waterton",
        text = "Windy but beautiful.",
    )

    // Pretend Firestore with the campsite and the reviews still in the app
    private var campsite by mutableStateOf<Campsite?>(null)
    private val reviews = mutableStateListOf<Review>()

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    // Set to true to pretend Firestore failed to remove the review
    private var removeFails = false

    private var retryTapped = false
    private var backTapped = false

    // Show a fresh reviews screen before every single test
    @Before
    fun setUp() {
        campsite = Campsite(
            id = "site-lake-louise",
            ownerId = "owner-device-7f3a",
            name = "Lake Louise Site 12",
            location = "Banff, AB",
        )
        reviews.clear()
        reviews.addAll(listOf(goodReview, rudeReview, watertonReview))
        isLoading = false
        errorMessage = null
        removeFails = false
        retryTapped = false
        backTapped = false

        composeTestRule.setContent {
            AdminCampsiteReviewsScreen(
                campsite = campsite,
                reviews = reviews,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onRetry = { retryTapped = true },
                onBack = { backTapped = true },
                onRemoveReview = { review, showError ->
                    // Remove it like Firestore would, or show the error if removing fails
                    if (removeFails) {
                        showError("Could not remove review. Try again.")
                    } else {
                        reviews.remove(review)
                    }
                },
            )
        }
    }

    @Test
    fun campsiteReviews_reviewsExist_onlyThisCampsiteListed() {
        // verify this campsite's reviews show
        composeTestRule.onNodeWithText("Reviews for Lake Louise Site 12").assertIsDisplayed()
        composeTestRule.onNodeWithText("Great views of the lake and a quiet site.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rude review that breaks the app policy.").assertIsDisplayed()

        // verify the other campsite's review is not listed
        composeTestRule.onNodeWithTag("reviewCard-review-3").assertDoesNotExist()
    }

    @Test
    fun removeReview_confirmed_reviewGone() {
        // remove the rude review
        composeTestRule.onNodeWithTag("removeReviewButton-review-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveReviewButton").performClick()

        // verify it's gone and the other review is still there
        composeTestRule.onNodeWithText("Rude review that breaks the app policy.").assertDoesNotExist()
        composeTestRule.onNodeWithTag("reviewCard-review-1").assertIsDisplayed()
    }

    @Test
    fun removeReview_lastReviewRemoved_noReviewsShown() {
        // remove both reviews
        composeTestRule.onNodeWithTag("removeReviewButton-review-1").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveReviewButton").performClick()
        composeTestRule.onNodeWithTag("removeReviewButton-review-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveReviewButton").performClick()

        // verify the empty message shows
        composeTestRule.onNodeWithText("No reviews for this campsite yet.").assertIsDisplayed()
    }

    @Test
    fun removeReview_cancelled_reviewKept() {
        // tap Remove, then change your mind
        composeTestRule.onNodeWithTag("removeReviewButton-review-2").performClick()
        composeTestRule.onNodeWithTag("cancelRemoveReviewButton").performClick()

        // verify the review is still listed
        composeTestRule.onNodeWithTag("reviewCard-review-2").assertIsDisplayed()
    }

    @Test
    fun removeReview_firestoreFails_errorShown() {
        // Firestore will fail to remove
        removeFails = true

        composeTestRule.onNodeWithTag("removeReviewButton-review-2").performClick()
        composeTestRule.onNodeWithTag("confirmRemoveReviewButton").performClick()

        // verify the error shows and the review is still listed
        composeTestRule.onNodeWithText("Could not remove review. Try again.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reviewCard-review-2").assertIsDisplayed()
    }

    @Test
    fun campsiteReviews_stillLoading_loadingShown() {
        // Firestore has not answered yet
        isLoading = true

        // verify loading shows instead of the reviews
        composeTestRule.onNodeWithText("Loading reviews…").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reviewCard-review-1").assertDoesNotExist()
    }

    @Test
    fun campsiteReviews_loadFails_retryShown() {
        // Firestore failed to load
        errorMessage = "Failed to load reviews"

        // verify the error shows and Retry asks to load again
        composeTestRule.onNodeWithText("Unable to load reviews. Please try again.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryTapped)
    }

    @Test
    fun campsiteReviews_campsiteRemoved_notFoundShown() {
        // the campsite was removed while its reviews were open
        campsite = null

        // verify a not found message shows instead of reviews
        composeTestRule.onNodeWithText("Campsite not found. It may have been removed.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reviewCard-review-1").assertDoesNotExist()
    }

    @Test
    fun campsiteReviews_backTapped_goesBack() {
        // tap Back
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify going back was asked for
        assertTrue(backTapped)
    }
}
