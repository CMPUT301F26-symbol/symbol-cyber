/*
 * NotificationPreferenceIntentTest.kt
 * UI tests for US 01.10.01: opt out/in, save-in-progress, and save failure display.
 */
package com.example.code.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
class NotificationPreferenceIntentTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun user(enabled: Boolean) = User(
        deviceId = "entrant-23",
        notificationsEnabled = enabled,
        roles = setOf(UserRole.ENTRANT),
    )

    @Test
    fun switchSupportsOptOutAndOptIn() {
        val current = mutableStateOf(user(true))
        val requested = mutableListOf<Boolean>()
        composeRule.setContent {
            ProfileScreen(
                user = current.value,
                activeMode = AppMode.ENTRANT,
                availableModes = listOf(AppMode.ENTRANT),
                isSaving = false,
                errorMessage = null,
                onSaveProfile = { _, _, _ -> },
                onSwitchMode = {},
                onNotificationPreferenceChange = {
                    requested.add(it)
                    current.value = user(it)
                },
            )
        }
        val toggle = composeRule.onNodeWithTag("notificationPreferenceSwitch")
        toggle.performScrollTo().assertIsOn().performClick()
        toggle.assertIsOff().performClick()
        toggle.assertIsOn()
        composeRule.runOnIdle { assertEquals(listOf(false, true), requested) }
    }

    @Test
    fun switchDisabledWhileSaving() {
        composeRule.setContent {
            ProfileScreen(
                user = user(false),
                activeMode = AppMode.ENTRANT,
                availableModes = listOf(AppMode.ENTRANT),
                isSaving = true,
                errorMessage = null,
                onSaveProfile = { _, _, _ -> },
                onSwitchMode = {},
                onNotificationPreferenceChange = {},
            )
        }
        composeRule.onNodeWithTag("notificationPreferenceSwitch")
            .performScrollTo().assertIsOff().assertIsNotEnabled()
    }

    @Test
    fun failedSaveDisplaysErrorAndKeepsSavedSwitchState() {
        val error = mutableStateOf<String?>(null)
        composeRule.setContent {
            ProfileScreen(
                user = user(true),
                activeMode = AppMode.ENTRANT,
                availableModes = listOf(AppMode.ENTRANT),
                isSaving = false,
                errorMessage = error.value,
                onSaveProfile = { _, _, _ -> },
                onSwitchMode = {},
                onNotificationPreferenceChange = { error.value = "Failed to save notification preference" },
            )
        }
        val toggle = composeRule.onNodeWithTag("notificationPreferenceSwitch")
        toggle.performScrollTo().performClick()
        toggle.assertIsOn()
        composeRule.onNodeWithText("Failed to save notification preference")
            .performScrollTo().assertIsDisplayed()
    }
}
