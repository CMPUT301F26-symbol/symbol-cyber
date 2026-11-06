/*
 * ProfileIntentTest.kt
 * UI tests for US 01.07.03 profile editing.
 * Verifies that profile fields can be edited and that save actions receive
 * the entered name, email, and phone number.
 */

package com.example.code.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.User
import com.example.code.UserRole
import com.example.code.navigation.AppMode
import com.example.code.ui.common.ProfileScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val user =
        User(
            deviceId = "device-123",
            name = "Old Name",
            email = "old@example.com",
            phoneNumber = "7805550000",
            roles = setOf(UserRole.ENTRANT),
        )

    @Test
    fun profileDisplaysExistingInformation() {
        composeRule.setContent {
            ProfileScreen(
                user = user,
                activeMode = AppMode.ENTRANT,
                availableModes = listOf(AppMode.ENTRANT),
                isSaving = false,
                errorMessage = null,
                onSaveProfile = { _, _, _ -> },
                onSwitchMode = {},
            )
        }

        composeRule.onNodeWithText("Old Name").assertExists()
        composeRule.onNodeWithText("old@example.com").assertExists()
        composeRule.onNodeWithText("7805550000").assertExists()
    }

    @Test
    fun editingProfilePassesUpdatedValuesToSave() {
        var savedName = ""
        var savedEmail = ""
        var savedPhone = ""

        composeRule.setContent {
            ProfileScreen(
                user = user,
                activeMode = AppMode.ENTRANT,
                availableModes = listOf(AppMode.ENTRANT),
                isSaving = false,
                errorMessage = null,
                onSaveProfile = { name, email, phone ->
                    savedName = name
                    savedEmail = email
                    savedPhone = phone
                },
                onSwitchMode = {},
            )
        }

        composeRule.onNodeWithText("Old Name")
            .performTextClearance()
        composeRule.onNodeWithText("Name")
            .performTextInput("Amr Soliman")

        composeRule.onNodeWithText("old@example.com")
            .performTextClearance()
        composeRule.onNodeWithText("Email")
            .performTextInput("amr@example.com")

        composeRule.onNodeWithText("7805550000")
            .performTextClearance()
        composeRule.onNodeWithText("Phone number")
            .performTextInput("7805551234")

        composeRule.onNodeWithText("Save").performClick()

        assertEquals("Amr Soliman", savedName)
        assertEquals("amr@example.com", savedEmail)
        assertEquals("7805551234", savedPhone)
    }

    @Test
    fun profileDisplaysValidationError() {
        composeRule.setContent {
            ProfileScreen(
                user = user,
                activeMode = AppMode.ENTRANT,
                availableModes = listOf(AppMode.ENTRANT),
                isSaving = false,
                errorMessage = "Please enter a valid email address.",
                onSaveProfile = { _, _, _ -> },
                onSwitchMode = {},
            )
        }

        composeRule
            .onNodeWithText("Please enter a valid email address.")
            .assertExists()
    }
}