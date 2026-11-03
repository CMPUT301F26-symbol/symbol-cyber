package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.code.model.Campsite
import com.example.code.model.CampsiteFilters
import com.example.code.ui.entrant.ExploreScreen
import org.junit.Rule
import org.junit.Test

class ExploreFiltersTest {
    @get:Rule val compose = createComposeRule()

    @Test fun listUpdatesWithAppliedFiltersAndNewRecords() {
        val filters = mutableStateOf(CampsiteFilters(location = "Banff", guestCount = 4))
        val campsites = mutableStateOf(listOf(
            Campsite(id = "1", name = "Pine", location = "Banff", maxOccupancy = 4),
            Campsite(id = "2", name = "Lake", location = "Jasper", maxOccupancy = 6),
        ))
        compose.setContent {
            MaterialTheme { ExploreScreen(campsites.value, {}, filters = filters.value) }
        }
        compose.onNodeWithText("Pine").assertIsDisplayed()
        compose.onNodeWithText("Lake").assertDoesNotExist()
        compose.runOnIdle { filters.value = CampsiteFilters(location = "Jasper") }
        compose.onNodeWithText("Lake").assertIsDisplayed()
        compose.onNodeWithText("Pine").assertDoesNotExist()
        compose.runOnIdle { campsites.value = emptyList() }
        compose.onNodeWithText("No campsites match your current filters.").assertIsDisplayed()
        compose.onNodeWithText("Lake").assertDoesNotExist()
    }
}
