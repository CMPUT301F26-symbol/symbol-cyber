// UI tests for the admin viewing one profile's information.
// AI use: Claude (Anthropic) drafted these tests; reviewed and run by Fahad.
package com.example.code.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.admin.AdminProfile
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminProfileDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val avery = AdminProfile(
        id = "device-avery",
        name = "Avery Chen",
        email = "avery@example.com",
        phone = "780-555-0142",
    )

    // The profile on screen, changed by each test
    private var profile by mutableStateOf<AdminProfile?>(null)
    private var isOwnProfile by mutableStateOf(false)

    private var backTapped = false

    // Show a fresh details screen before every single test
    @Before
    fun setUp() {
        profile = avery
        isOwnProfile = false
        backTapped = false

        composeTestRule.setContent {
            AdminProfileDetailsScreen(
                profile = profile,
                isOwnProfile = isOwnProfile,
                onBack = { backTapped = true },
            )
        }
    }

    @Test
    fun profileDetails_allFields_allShown() {
        // verify every piece of information is shown
        composeTestRule.onNodeWithText("Avery Chen").assertIsDisplayed()
        composeTestRule.onNodeWithText("avery@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("780-555-0142").assertIsDisplayed()
        composeTestRule.onNodeWithText("device-avery").assertIsDisplayed()

        // verify it is not marked as the admin's own
        composeTestRule.onNodeWithText("Your profile").assertDoesNotExist()
    }

    @Test
    fun profileDetails_blankFields_notProvidedShown() {
        // a new device that has not filled in its profile yet
        profile = AdminProfile(id = "device-new-91bc")

        // verify blank fields say so instead of showing nothing
        composeTestRule.onNodeWithText("Unnamed profile").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Not provided").assertCountEquals(2)
        composeTestRule.onNodeWithText("device-new-91bc").assertIsDisplayed()
    }

    @Test
    fun profileDetails_ownProfile_yourProfileShown() {
        // the admin opens their own profile
        isOwnProfile = true

        // verify it is marked as theirs
        composeTestRule.onNodeWithText("Your profile").assertIsDisplayed()
    }

    @Test
    fun profileDetails_profileRemoved_notFoundShown() {
        // the profile was removed while it was open
        profile = null

        // verify the admin is told it is gone
        composeTestRule.onNodeWithText("Profile not found. It may have been removed.").assertIsDisplayed()
    }

    @Test
    fun profileDetails_backTapped_onBackCalled() {
        // tap Back
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify the screen asked to go back
        assertTrue(backTapped)
    }
}
