package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.code.model.Campsite
import com.example.code.ui.entrant.ExploreScreen
import org.junit.Rule
import org.junit.Test

class ExploreSearchTest {
    @get:Rule val compose = createComposeRule()

    @Test fun searchThenClearRestoresAllCampsites() {
        compose.setContent {
            MaterialTheme {
                ExploreScreen(
                    campsites = listOf(
                        Campsite(id = "1", name = "Pine Ridge", location = "Banff"),
                        Campsite(id = "2", name = "Jasper Lake", location = "Jasper"),
                    ),
                    onCampsiteClick = {},
                )
            }
        }
        compose.onNodeWithText("Search campsites").performTextInput("Banff")
        compose.onNodeWithText("Pine Ridge").assertIsDisplayed()
        compose.onNodeWithText("Jasper Lake").assertDoesNotExist()
        compose.onNodeWithText("Clear search").performClick()
        compose.onNodeWithText("Jasper Lake").assertIsDisplayed()
        compose.onNodeWithText("Search campsites").performTextInput("unknown")
        compose.onNodeWithText("No results found. Try another name or location.").assertIsDisplayed()
    }
}
