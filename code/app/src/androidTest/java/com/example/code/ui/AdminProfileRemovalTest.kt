/*
 * AdminProfileRemovalTest.kt
 * Checks confirmation/cancel, removal, failure and denied access with a fake profile store.
 * Known issues: does not mutate shared Firestore data or provision live administrator claims.
 */
package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.code.admin.AdminProfile
import com.example.code.admin.AdminProfileController
import com.example.code.admin.AdminProfileRepository
import com.example.code.ui.admin.AdminProfileRemovalRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AdminProfileRemovalTest {
    @get:Rule val compose = createComposeRule()
    private class FakeProfiles : AdminProfileRepository {
        val rows = MutableStateFlow(listOf(
            AdminProfile("admin-device", "Dean Admin", "dean@example.com"),
            AdminProfile("device-avery", "Avery Chen", "avery@example.com"),
        ))
        var calls = 0
        var failure = false
        override fun observeProfiles(): Flow<List<AdminProfile>> = rows
        override suspend fun removeProfile(profileId: String): Boolean {
            calls++
            if (failure) error("offline")
            val exists = rows.value.any { it.id == profileId }
            rows.value = rows.value.filterNot { it.id == profileId }
            return exists
        }
    }

    @Test fun cancellationDoesNotDeleteAndConfirmationRemovesTheSelectedProfile() {
        val repo = FakeProfiles()
        compose.setContent { MaterialTheme { AdminProfileRemovalRoute("admin-device", AdminProfileController(repo, { true }, "admin-device")) } }
        compose.onNodeWithText("Avery Chen").assertIsDisplayed()
        compose.onNodeWithText("Your profile").assertIsDisplayed()
        compose.onNodeWithText("Remove").performClick()
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertEquals(0, repo.calls) }
        compose.onNodeWithText("Avery Chen").assertIsDisplayed()
        compose.onNodeWithText("Remove").performClick()
        compose.onNodeWithText("Confirm removal").performClick()
        compose.onNodeWithText("Profile removed.").assertIsDisplayed()
        compose.onNodeWithText("Avery Chen").assertDoesNotExist()
        compose.onNodeWithText("Dean Admin").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, repo.calls) }
    }

    @Test fun failedDeletionKeepsTheProfileAndCanBeRetried() {
        val repo = FakeProfiles().apply { failure = true }
        compose.setContent { MaterialTheme { AdminProfileRemovalRoute("admin-device", AdminProfileController(repo, { true }, "admin-device")) } }
        compose.onNodeWithText("Remove").performClick()
        compose.onNodeWithText("Confirm removal").performClick()
        compose.onNodeWithText("Could not remove the profile. It is still available; try again.").assertIsDisplayed()
        compose.onNodeWithText("Avery Chen").assertIsDisplayed()
        compose.runOnIdle { repo.failure = false }
        compose.onNodeWithText("Remove").performClick()
        compose.onNodeWithText("Confirm removal").performClick()
        compose.onNodeWithText("Avery Chen").assertDoesNotExist()
    }

    @Test fun deniedAdminAccessDoesNotExposeProfilesOrRemovalControls() {
        val repo = FakeProfiles()
        compose.setContent { MaterialTheme { AdminProfileRemovalRoute("admin-device", AdminProfileController(repo, { false }, "admin-device")) } }
        compose.onNodeWithText("Verified administrator access is required to manage profiles.").assertIsDisplayed()
        compose.onNodeWithText("Avery Chen").assertDoesNotExist()
        compose.onNodeWithText("Remove").assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, repo.calls) }
    }
}
