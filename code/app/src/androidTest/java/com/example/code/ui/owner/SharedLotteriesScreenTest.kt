// UI tests for owners viewing shared lotteries and changing only their own campsites.
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
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.LotteryCampsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedLotteriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_800_000_000_000L

    private val lakeLouise = Campsite(id = "site-lake-louise", ownerId = "owner-device-7f3a", name = "Lake Louise Site 12")
    private val waterton = Campsite(id = "site-waterton", ownerId = "other-owner-device-91bc", name = "Waterton Lakeshore")
    private val cameron = Campsite(id = "site-cameron", ownerId = "other-owner-device-91bc", name = "Cameron Lake Site 7")

    // The first owner's lottery, which the second owner already added Waterton to
    private val sharedLottery = Lottery(
        id = "lottery-summer",
        campsiteId = lakeLouise.id,
        campsiteName = lakeLouise.name,
        ownerId = "owner-device-7f3a",
        registrationStart = now + day,
        registrationEnd = now + 2 * day,
        bookingStart = now + 3 * day,
        bookingEnd = now + 5 * day,
        campsites = listOf(
            LotteryCampsite(lakeLouise.id, lakeLouise.name),
            LotteryCampsite(waterton.id, waterton.name),
        ),
    )

    // The owner using the screen and the lotteries in the app
    private var ownerId by mutableStateOf("other-owner-device-91bc")
    private var lotteries by mutableStateOf(listOf(sharedLottery))

    private var added: Pair<Lottery, Campsite>? = null
    private var removed: Pair<Lottery, Campsite>? = null
    private var backTapped = false

    // Show a fresh shared lotteries screen before every single test
    @Before
    fun setUp() {
        ownerId = "other-owner-device-91bc"
        lotteries = listOf(sharedLottery)
        added = null
        removed = null
        backTapped = false

        composeTestRule.setContent {
            SharedLotteriesScreen(
                lotteries = lotteries,
                campsites = listOf(lakeLouise, waterton, cameron),
                ownerId = ownerId,
                now = now,
                onAddCampsite = { lottery, campsite -> added = lottery to campsite },
                onRemoveCampsite = { lottery, campsite -> removed = lottery to campsite },
                onBack = { backTapped = true },
            )
        }
    }

    @Test
    fun sharedLotteries_secondOwner_seesBothOwnersCampsites() {
        // verify the lottery shows campsites from both owners, marked by whose they are
        composeTestRule.onNodeWithText("Created by another owner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lake Louise Site 12 (another owner's)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Waterton Lakeshore (yours)").assertIsDisplayed()
    }

    @Test
    fun sharedLotteries_secondOwner_canOnlyRemoveOwnCampsite() {
        // verify only the second owner's campsite has a Remove button
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-waterton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-lake-louise").assertDoesNotExist()

        // remove it
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-waterton").performClick()

        // verify that campsite was asked to be removed
        assertEquals(sharedLottery to waterton, removed)
    }

    @Test
    fun sharedLotteries_addTapped_ownCampsiteAdded() {
        // add another of the second owner's campsites
        composeTestRule.onNodeWithTag("addCampsite-lottery-summer-site-cameron").performClick()

        // verify that campsite was asked to be added to this lottery
        assertEquals(sharedLottery to cameron, added)
    }

    @Test
    fun sharedLotteries_creator_cantChangeOtherOwnersCampsite() {
        // the owner who created the lottery opens the screen
        ownerId = "owner-device-7f3a"

        // verify they can remove their own campsite but not the other owner's
        composeTestRule.onNodeWithText("Created by you").assertIsDisplayed()
        composeTestRule.onNodeWithText("Waterton Lakeshore (another owner's)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-lake-louise").assertIsDisplayed()
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-waterton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("addCampsite-lottery-summer-site-cameron").assertDoesNotExist()
    }

    @Test
    fun sharedLotteries_entrantsJoined_noChangesAllowed() {
        // an entrant joined, so campsites are locked
        lotteries = listOf(sharedLottery.copy(waitingListCount = 1))

        // verify the reason shows and there are no buttons to change campsites
        composeTestRule.onNodeWithText("Campsites can't change once entrants join or registration closes.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("removeCampsite-lottery-summer-site-waterton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("addCampsite-lottery-summer-site-cameron").assertDoesNotExist()
    }

    @Test
    fun sharedLotteries_noLotteries_emptyStateShown() {
        // nobody has made a lottery yet
        lotteries = emptyList()

        // verify the empty message shows
        composeTestRule.onNodeWithText("No lotteries are open for sharing right now.").assertIsDisplayed()
    }

    @Test
    fun sharedLotteries_backTapped_goesBack() {
        // tap Back
        composeTestRule.onNodeWithTag("backButton").performClick()

        // verify going back was asked for
        assertTrue(backTapped)
    }
}
