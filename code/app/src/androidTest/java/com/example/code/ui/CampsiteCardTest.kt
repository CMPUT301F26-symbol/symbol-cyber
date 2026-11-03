package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.code.model.Campsite
import com.example.code.ui.entrant.CampsiteCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CampsiteCardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun showsSummaryOpensSelectedRecordAndReflectsUpdates() {
        val site = mutableStateOf(Campsite(id = "1", name = "Pine", location = "Banff", maxOccupancy = 4))
        var selectedId: String? = null
        compose.setContent {
            MaterialTheme { CampsiteCard(site.value) { selectedId = site.value.id } }
        }
        compose.onNodeWithText("Pine").assertIsDisplayed()
        compose.onNodeWithText("Banff").assertIsDisplayed()
        compose.onNodeWithText("Maximum occupancy: 4").assertIsDisplayed()
        compose.onNodeWithText("No ratings yet").assertIsDisplayed()
        compose.onNodeWithText("Pine").performClick()
        compose.runOnIdle {
            assertEquals("1", selectedId)
            site.value = site.value.copy(name = "Updated Pine", maxOccupancy = 6, rating = 4.5)
        }
        compose.onNodeWithText("Updated Pine").assertIsDisplayed()
        compose.onNodeWithText("Maximum occupancy: 6").assertIsDisplayed()
        compose.onNodeWithText("No ratings yet").assertDoesNotExist()
        compose.onNodeWithText("Rating:", substring = true).assertIsDisplayed()
    }
}
