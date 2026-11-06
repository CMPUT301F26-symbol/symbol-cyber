// Intent tests for the admin browsing profiles and opening one to see its information.
package com.example.code.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.admin.AdminProfile
import com.example.code.admin.AdminProfileController
import com.example.code.admin.AdminProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminProfilesIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val adminId = "admin-device-4d2e"

    // Pretend Firestore: profiles saved in the app
    private class FakeProfiles : AdminProfileRepository {
        val rows = MutableStateFlow(emptyList<AdminProfile>())

        override fun observeProfiles(): Flow<List<AdminProfile>> = rows

        override suspend fun removeProfile(profileId: String): Boolean {
            rows.value = rows.value.filterNot { it.id == profileId }
            return true
        }
    }

    private val profiles = FakeProfiles()

    // Start on the admin's profile list before every single test
    @Before
    fun setUp() {
        profiles.rows.value = listOf(
            AdminProfile(adminId, "Dana Admin", "dana@example.com"),
            AdminProfile("device-avery", "Avery Chen", "avery@example.com", "780-555-0142"),
            AdminProfile("device-jordan", "Jordan Lee", "jordan@example.com"),
        )

        composeTestRule.setContent {
            AdminProfileRemovalRoute(
                currentProfileId = adminId,
                // Pretend the admin check passed
                controller = AdminProfileController(profiles, { true }, adminId),
            )
        }
    }

    @Test
    fun browseProfiles_profilesExist_allListed() {
        // verify every profile is listed
        composeTestRule.onNodeWithText("Dana Admin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Avery Chen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jordan Lee").assertIsDisplayed()
    }

    @Test
    fun openProfile_profileTapped_detailsShownAndBack() {
        // the admin taps a profile in the list
        composeTestRule.onNodeWithTag("profileCard-device-avery").performClick()

        // verify its information is shown
        composeTestRule.onNodeWithText("Avery Chen").assertIsDisplayed()
        composeTestRule.onNodeWithText("780-555-0142").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jordan Lee").assertDoesNotExist()

        // go back to the list
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify the whole list shows again
        composeTestRule.onNodeWithText("Profiles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jordan Lee").assertIsDisplayed()
    }

    @Test
    fun openProfile_removedWhileOpen_notFoundShown() {
        // the admin opens a profile
        composeTestRule.onNodeWithTag("profileCard-device-avery").performClick()

        // someone else removes it while it is open
        composeTestRule.runOnIdle {
            profiles.rows.value = profiles.rows.value.filterNot { it.id == "device-avery" }
        }

        // verify the admin is told it is gone
        composeTestRule.onNodeWithText("Profile not found. It may have been removed.").assertIsDisplayed()
    }
}
