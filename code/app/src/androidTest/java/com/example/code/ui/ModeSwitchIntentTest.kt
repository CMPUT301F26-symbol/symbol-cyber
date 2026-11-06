/*
 * ModeSwitchIntentTest.kt
 * UI tests for US 03.11.01 multirole mode switching.
 */

package com.example.code.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.navigation.AppMode
import com.example.code.ui.common.ProfileScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModeSwitchIntentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun profileShowsAllAssignedModes() {
        composeRule.setContent {
            ProfileScreen(
                activeMode = AppMode.ADMIN,
                availableModes = listOf(
                    AppMode.ENTRANT,
                    AppMode.OWNER,
                    AppMode.ADMIN,
                ),
                onSwitchMode = {},
            )
        }

        composeRule
            .onNodeWithText("Entrant")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Owner")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Admin")
            .assertIsDisplayed()
    }

    @Test
    fun selectingAnotherModeRequestsThatMode() {
        var selectedMode: AppMode? = null

        composeRule.setContent {
            ProfileScreen(
                activeMode = AppMode.ADMIN,
                availableModes = listOf(
                    AppMode.ENTRANT,
                    AppMode.OWNER,
                    AppMode.ADMIN,
                ),
                onSwitchMode = {
                    selectedMode = it
                },
            )
        }

        composeRule
            .onNodeWithText("Owner")
            .performClick()

        assertEquals(
            AppMode.OWNER,
            selectedMode,
        )
    }

    @Test
    fun unavailableModesAreNotDisplayed() {
        composeRule.setContent {
            ProfileScreen(
                activeMode = AppMode.ADMIN,
                availableModes = listOf(
                    AppMode.ADMIN,
                ),
                onSwitchMode = {},
            )
        }

        composeRule
            .onNodeWithText("Admin")
            .assertExists()

        composeRule
            .onNodeWithText("Entrant")
            .assertDoesNotExist()

        composeRule
            .onNodeWithText("Owner")
            .assertDoesNotExist()
    }
}