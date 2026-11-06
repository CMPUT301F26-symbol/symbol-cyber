package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.code.model.Campsite
import com.example.code.model.ownedCampsites
import com.example.code.ui.owner.OwnerCampsiteDetailsDestination
import com.example.code.ui.owner.OwnerCampsitesScreen
import org.junit.Rule
import org.junit.Test

class OwnerCampsiteDetailsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun opensOwnedRecordShowsLatestSavedDetailsAndReturnsToList() {
        val sites = mutableStateOf(listOf(
            Campsite(id = "mine", ownerId = "owner-a", name = "Pine", description = "Old amenities"),
            Campsite(id = "other", ownerId = "owner-b", name = "Other owner's site"),
        ))
        val selected = mutableStateOf<String?>(null)
        compose.setContent {
            MaterialTheme {
                if (selected.value == null) {
                    OwnerCampsitesScreen(
                        ownedCampsites(sites.value, "owner-a"), {},
                        onCampsiteClick = { selected.value = it.id },
                    )
                } else {
                    OwnerCampsiteDetailsDestination(selected.value, "owner-a", sites.value, false, null, {}, { selected.value = null })
                }
            }
        }
        compose.onNodeWithText("Other owner's site").assertDoesNotExist()
        compose.onNodeWithText("Pine").performClick()
        compose.onNodeWithText("Old amenities").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { sites.value = sites.value.map { if (it.id == "mine") it.copy(description = "Latest amenities") else it } }
        compose.onNodeWithText("Latest amenities").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("My campsites").assertIsDisplayed()
    }

    @Test fun cannotOpenAnotherOwnersRecordOrKeepItAfterOwnershipChanges() {
        val site = mutableStateOf(Campsite(id = "1", ownerId = "owner-a", name = "Pine"))
        compose.setContent {
            MaterialTheme { OwnerCampsiteDetailsDestination("1", "owner-a", listOf(site.value), false, null, {}, {}) }
        }
        compose.onNodeWithText("Pine").assertIsDisplayed()
        compose.runOnIdle { site.value = site.value.copy(ownerId = "owner-b") }
        compose.onNodeWithText("Pine").assertDoesNotExist()
        compose.onNodeWithText("Campsite not found. It may have been removed.").assertIsDisplayed()
    }
}
