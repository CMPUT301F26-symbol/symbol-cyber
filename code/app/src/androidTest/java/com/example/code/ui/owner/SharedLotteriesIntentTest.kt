// Intent tests for two owners sharing one lottery, each changing only their own campsites.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.ui.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.code.controller.SharedLotteryController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedLotteriesIntentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_800_000_000_000L

    private val lakeLouise = Campsite(id = "site-lake-louise", ownerId = "owner-device-7f3a", name = "Lake Louise Site 12")
    private val waterton = Campsite(id = "site-waterton", ownerId = "other-owner-device-91bc", name = "Waterton Lakeshore")

    // Pretend Firestore with the one lottery the first owner created
    private var lottery by mutableStateOf(Lottery())

    // Which owner is using the app right now
    private var ownerId by mutableStateOf("other-owner-device-91bc")

    // Start with the second owner looking at the first owner's lottery
    @Before
    fun setUp() {
        lottery = Lottery(
            id = "lottery-summer",
            campsiteId = lakeLouise.id,
            campsiteName = lakeLouise.name,
            ownerId = "owner-device-7f3a",
            registrationStart = now + day,
            registrationEnd = now + 2 * day,
            bookingStart = now + 3 * day,
            bookingEnd = now + 5 * day,
        )
        ownerId = "other-owner-device-91bc"

        composeTestRule.setContent {
            SharedLotteriesScreen(
                lotteries = listOf(lottery),
                campsites = listOf(lakeLouise, waterton),
                ownerId = ownerId,
                now = now,
                onAddCampsite = { current, campsite ->
                    // Save it like the app does
                    lottery = SharedLotteryController.addCampsite(current, campsite, ownerId, now)
                },
                onRemoveCampsite = { current, campsite ->
                    lottery = SharedLotteryController.removeCampsite(current, campsite, ownerId, now)
                },
            )
        }
    }

    @Test
    fun shareLottery_secondOwnerAdds_creatorSeesItButCantRemoveIt() {
        // the second owner adds Waterton to the lottery
        composeTestRule.onNodeWithTag("addCampsite-lottery-summer-site-waterton").performClick()

        // verify the lottery now has both owners' campsites
        composeTestRule.onNodeWithText("Lake Louise Site 12 (another owner's)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Waterton Lakeshore (yours)").assertIsDisplayed()

        // the first owner opens the app
        ownerId = "owner-device-7f3a"

        // verify they see Waterton in their lottery but can't remove it
        composeTestRule.onNodeWithText("Waterton Lakeshore (another owner's)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-waterton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-lake-louise").assertIsDisplayed()
    }

    @Test
    fun shareLottery_secondOwnerRemovesOwn_onlyCreatorsCampsiteLeft() {
        // the second owner adds Waterton, then takes it back out
        composeTestRule.onNodeWithTag("addCampsite-lottery-summer-site-waterton").performClick()
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-waterton").performClick()

        // verify Waterton is out and can be added again, while Lake Louise stays
        composeTestRule.onNodeWithText("Lake Louise Site 12 (another owner's)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Waterton Lakeshore (yours)").assertDoesNotExist()
        composeTestRule.onNodeWithTag("addCampsite-lottery-summer-site-waterton").assertIsDisplayed()
    }
}
