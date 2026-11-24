/*
 * MultiCampsiteLotteryUiTest.kt
 * Purpose: Android Compose UI tests for the owner's multiple-campsite lottery flow.
 * Verifies campsite checkbox selection, exclusion of other owners' campsites,
 * empty-selection validation and identification of included campsites.
 * User story: US 02.04.02.
 * Uses supplied data and callbacks; does not write to the live Firebase database.
 * Existing lottery UI tests continue to cover native date/time picker publication.
 * Run from androidTest on an Android emulator or device.
 */


package com.example.code.ui.owner

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.LotteryCampsite
import org.junit.Rule
import org.junit.Test

// === US 02.04.02: BEGIN owner multiple campsite UI tests ===
class MultiCampsiteLotteryUiTest {
    @get:Rule
    val compose = createComposeRule()

    private val first = Campsite(id = "site-1", ownerId = "owner-device", name = "Pine Creek")
    private val second = Campsite(id = "site-2", ownerId = "owner-device", name = "River Bend")

    @Test
    fun ownerCanSelectMultipleCampsitesButCannotSeeOtherOwnersSites() {
        val otherOwnersSite = Campsite(
            id = "site-3", ownerId = "different-owner", name = "Other owner's campsite",
        )
        compose.setContent {
            MaterialTheme {
                CreateLotteryScreen(
                    campsite = first,
                    ownerCampsites = listOf(first, second, otherOwnersSite),
                    onPublish = { "created-lottery" },
                    onPublished = {},
                    onCancel = {},
                )
            }
        }
        compose.onNodeWithTag("lottery-campsite-site-1").assertIsOn()
        compose.onNodeWithTag("lottery-campsite-site-2")
            .assertIsOff().performScrollTo().performClick().assertIsOn()
        compose.onNodeWithText("Selected: 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("lottery-campsite-site-3").assertDoesNotExist()
    }

    @Test
    fun publishingWithoutAnySelectedCampsiteShowsAnError() {
        compose.setContent {
            MaterialTheme {
                CreateLotteryScreen(
                    campsite = first,
                    ownerCampsites = listOf(first, second),
                    onPublish = { error("An empty selection must not be saved") },
                    onPublished = {},
                    onCancel = {},
                )
            }
        }
        compose.onNodeWithTag("lottery-campsite-site-1").performClick().assertIsOff()
        compose.onNodeWithText("Publish").performScrollTo().performClick()
        compose.onNodeWithText("Select at least one campsite.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test
    fun ownerCanIdentifyAllCampsitesIncludedInALottery() {
        val lottery = Lottery(
            id = "shared-lottery",
            campsiteId = first.id,
            campsiteName = first.name,
            ownerId = first.ownerId,
            campsites = listOf(
                LotteryCampsite(first.id, first.name),
                LotteryCampsite(second.id, second.name),
            ),
        )
        compose.setContent {
            MaterialTheme {
                OwnerLotteriesScreen(lotteries = listOf(lottery), onOpenWaitingList = {})
            }
        }
        compose.onNodeWithText("Included campsites (2)").assertIsDisplayed()
        compose.onNodeWithText("• Pine Creek").assertIsDisplayed()
        compose.onNodeWithText("• River Bend").assertIsDisplayed()
    }
}
// === US 02.04.02: END owner multiple campsite UI tests ===
