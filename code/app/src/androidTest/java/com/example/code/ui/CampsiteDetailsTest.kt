package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.code.model.Campsite
import com.example.code.ui.entrant.CampsiteDetailsDestination
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CampsiteDetailsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fullDetailsIncludePlaceholderAndLiveEditsAndRemoval() {
        val sites = mutableStateOf(listOf(Campsite(
            id = "1", name = "Pine", location = "Banff", description = "Water and firepit",
            maxOccupancy = 4, latitude = 51.1, longitude = -115.5, rating = 4.5,
            price = 35.0, currency = "CAD", tags = listOf("Lake"),
            rules = "Quiet after 10", cancellationPolicy = "Cancel 48 hours ahead",
        )))
        compose.setContent {
            MaterialTheme { CampsiteDetailsDestination("1", sites.value, false, null, {}, {}) }
        }
        compose.onNodeWithText("Pine").assertIsDisplayed()
        compose.onNodeWithContentDescription("No promotional image available").assertIsDisplayed()
        for (text in listOf("Banff", "Water and firepit", "4", "Price", "Rating", "51.1, -115.5", "Lake", "Quiet after 10", "Cancel 48 hours ahead")) {
            compose.onNodeWithText(text).performScrollTo().assertIsDisplayed()
        }
        compose.runOnIdle { sites.value = listOf(sites.value[0].copy(description = "New amenities")) }
        compose.onNodeWithText("New amenities").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { sites.value = emptyList() }
        compose.onNodeWithText("Campsite not found. It may have been removed.").assertIsDisplayed()
        compose.onNodeWithText("Back").assertIsDisplayed()
    }

    @Test fun loadingAndFailureAreNotReportedAsMissing() {
        val loading = mutableStateOf(true)
        val error = mutableStateOf<String?>(null)
        var retried = false
        compose.setContent {
            MaterialTheme { CampsiteDetailsDestination("1", emptyList(), loading.value, error.value, { retried = true }, {}) }
        }
        compose.onNodeWithText("Loading campsite details…").assertIsDisplayed()
        compose.onNodeWithText("Campsite not found. It may have been removed.").assertDoesNotExist()
        compose.runOnIdle { loading.value = false; error.value = "offline" }
        compose.onNodeWithText("Unable to load campsite details. Please try again.").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        compose.runOnIdle { assertTrue(retried) }
    }
}
