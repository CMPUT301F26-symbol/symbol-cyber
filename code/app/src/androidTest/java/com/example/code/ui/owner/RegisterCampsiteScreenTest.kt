// UI tests for registering a campsite.
package com.example.code.ui.owner

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.example.code.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class RegisterCampsiteScreenTest {

    // Clicks the register button (not the title, which has the same words)
    private fun ComposeUiTest.clickRegister() {
        onNode(hasText("Register campsite") and hasClickAction()).performClick()
    }

    @Test
    fun validCampsiteGetsRegistered() = runComposeUiTest {
        var savedCampsite: Campsite? = null

        setContent {
            RegisterCampsiteScreen(
                onRegister = { campsite, _ -> savedCampsite = campsite },
                onBack = {},
            )
        }

        // Fill in every required field
        onNodeWithText("Name").performTextInput("Lake Louise Site 12")
        onNodeWithText("Location").performTextInput("Banff, AB")
        onNodeWithText("Description / amenities").performTextInput("Fire pit, picnic table")
        onNodeWithText("Maximum occupancy").performTextInput("6")
        onNodeWithText("Latitude").performTextInput("51.4254")
        onNodeWithText("Longitude").performTextInput("-116.1773")
        clickRegister()

        // The campsite should be sent to be saved
        assertEquals("Lake Louise Site 12", savedCampsite?.name)
        assertEquals(6, savedCampsite?.maxOccupancy)
    }

    @Test
    fun missingNameIsBlocked() = runComposeUiTest {
        var savedCampsite: Campsite? = null

        setContent {
            RegisterCampsiteScreen(
                onRegister = { campsite, _ -> savedCampsite = campsite },
                onBack = {},
            )
        }

        // Fill in everything except the name
        onNodeWithText("Location").performTextInput("Banff, AB")
        onNodeWithText("Description / amenities").performTextInput("Fire pit, picnic table")
        onNodeWithText("Maximum occupancy").performTextInput("6")
        onNodeWithText("Latitude").performTextInput("51.4254")
        onNodeWithText("Longitude").performTextInput("-116.1773")
        clickRegister()

        // Nothing should be saved and the error should show
        assertNull(savedCampsite)
        onNodeWithText("Please complete all required fields with valid values.").assertExists()
    }
}
