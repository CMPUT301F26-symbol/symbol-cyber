/* Tests owner QR navigation, rendered code and stable link copying without touching real clipboard data. Known issues: none. */
package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.code.model.Campsite
import com.example.code.qr.CampsiteQrLink
import com.example.code.ui.owner.CampsiteQrScreen
import com.example.code.ui.owner.OwnerCampsiteDetailsDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CampsiteQrScreenTest {
    @get:Rule val compose = createComposeRule()
    private val site = Campsite(id = "jasper-riverside-2027", ownerId = "owner-dean", name = "Jasper Riverside", description = "Riverfront tent campsite", maxOccupancy = 6)

    @Test fun ownerOpensQrAndReturnsToExistingLotteryControls() {
        compose.setContent {
            MaterialTheme { OwnerCampsiteDetailsDestination(site.id, site.ownerId, listOf(site), false, null, {}, {}, onPublishLottery = { _, _ -> "lottery-id" }) }
        }
        compose.onNodeWithText("Create lottery").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Show promotional QR code").performScrollTo().performClick()
        waitForImage()
        compose.onNodeWithText("Back").performScrollTo().performClick()
        compose.onNodeWithText("Create lottery").performScrollTo().assertIsDisplayed()
    }

    @Test fun imageAndCopyButtonRepresentTheSameSavedCampsite() {
        var copied: String? = null
        compose.setContent { MaterialTheme { CampsiteQrScreen(site, site.ownerId, {}, onCopyLink = { copied = it }) } }
        waitForImage()
        compose.onNodeWithContentDescription("Promotional QR code for Jasper Riverside").assertIsDisplayed()
        compose.onNodeWithText("Copy campsite link").performScrollTo().performClick()
        compose.onNodeWithText("Campsite link copied").assertIsDisplayed()
        compose.runOnIdle { assertEquals(site.id, CampsiteQrLink.decode(copied.orEmpty())) }
    }

    private fun waitForImage() {
        compose.waitUntil(15000) {
            compose.onAllNodesWithContentDescription("Promotional QR code for Jasper Riverside").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
