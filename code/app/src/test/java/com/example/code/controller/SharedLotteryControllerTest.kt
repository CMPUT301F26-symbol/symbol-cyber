// Unit tests for owners adding and removing their own campsites in a shared lottery.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.controller

import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.LotteryCampsite
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedLotteryControllerTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_800_000_000_000L

    // Two owners, each with their own campsites
    private val lakeLouise = Campsite(id = "site-lake-louise", ownerId = "owner-device-7f3a", name = "Lake Louise Site 12")
    private val moraine = Campsite(id = "site-moraine", ownerId = "owner-device-7f3a", name = "Moraine Lake Site 3")
    private val waterton = Campsite(id = "site-waterton", ownerId = "other-owner-device-91bc", name = "Waterton Lakeshore")
    private val campsites = listOf(lakeLouise, moraine, waterton)

    // A lottery the first owner created for Lake Louise, opening tomorrow
    private val lottery = Lottery(
        id = "lottery-summer",
        campsiteId = lakeLouise.id,
        campsiteName = lakeLouise.name,
        ownerId = "owner-device-7f3a",
        registrationStart = now + day,
        registrationEnd = now + 2 * day,
        bookingStart = now + 3 * day,
        bookingEnd = now + 5 * day,
    )

    @Test
    fun addCampsite_otherOwner_lotteryIncludesBothOwners() {
        // the second owner adds Waterton to the first owner's lottery
        val shared = SharedLotteryController.addCampsite(lottery, waterton, "other-owner-device-91bc", now)

        // verify the lottery now has the campsites of both owners
        assertEquals(
            listOf(LotteryCampsite(lakeLouise.id, lakeLouise.name), LotteryCampsite(waterton.id, waterton.name)),
            LotteryController.includedCampsites(shared),
        )
        assertEquals("owner-device-7f3a", shared.ownerId)
    }

    @Test
    fun addCampsite_notYourCampsite_refused() {
        // the second owner tries to add the first owner's campsite
        val error = assertThrows(IllegalArgumentException::class.java) {
            SharedLotteryController.addCampsite(lottery, moraine, "other-owner-device-91bc", now)
        }

        // verify it was refused with a clear reason
        assertEquals("You can only add your own campsites.", error.message)
    }

    @Test
    fun addCampsite_alreadyIncluded_refused() {
        val shared = SharedLotteryController.addCampsite(lottery, waterton, "other-owner-device-91bc", now)

        // verify the same campsite can't be added twice
        assertThrows(IllegalArgumentException::class.java) {
            SharedLotteryController.addCampsite(shared, waterton, "other-owner-device-91bc", now)
        }
    }

    @Test
    fun addCampsite_entrantsJoined_refused() {
        // an entrant is already on the waiting list
        val joined = lottery.copy(waitingListCount = 1)

        // verify campsites can't change under them
        assertThrows(IllegalArgumentException::class.java) {
            SharedLotteryController.addCampsite(joined, waterton, "other-owner-device-91bc", now)
        }
    }

    @Test
    fun removeCampsite_ownCampsite_removed() {
        val shared = SharedLotteryController.addCampsite(lottery, waterton, "other-owner-device-91bc", now)

        // the second owner takes their campsite back out
        val result = SharedLotteryController.removeCampsite(shared, waterton, "other-owner-device-91bc", now)

        // verify only the first owner's campsite is left
        assertEquals(listOf(LotteryCampsite(lakeLouise.id, lakeLouise.name)), LotteryController.includedCampsites(result))
    }

    @Test
    fun removeCampsite_otherOwnersCampsite_refused() {
        val shared = SharedLotteryController.addCampsite(lottery, waterton, "other-owner-device-91bc", now)

        // the first owner tries to remove the second owner's campsite
        val error = assertThrows(IllegalArgumentException::class.java) {
            SharedLotteryController.removeCampsite(shared, waterton, "owner-device-7f3a", now)
        }

        // verify owners can only change their own campsites
        assertEquals("You can only remove your own campsites.", error.message)
    }

    @Test
    fun removeCampsite_lastCampsite_refused() {
        // verify a lottery can't be left with nothing in it
        assertThrows(IllegalArgumentException::class.java) {
            SharedLotteryController.removeCampsite(lottery, lakeLouise, "owner-device-7f3a", now)
        }
    }

    @Test
    fun canChangeCampsites_registrationClosed_false() {
        // verify nothing can change after registration ends
        assertTrue(SharedLotteryController.canChangeCampsites(lottery, now))
        assertFalse(SharedLotteryController.canChangeCampsites(lottery, now + 3 * day))
    }

    @Test
    fun campsitesOwnedIn_sharedLottery_onlyThatOwnersCampsites() {
        val shared = SharedLotteryController.addCampsite(lottery, waterton, "other-owner-device-91bc", now)

        // verify each owner only gets their own campsites back
        assertEquals(
            listOf(LotteryCampsite(waterton.id, waterton.name)),
            SharedLotteryController.campsitesOwnedIn(shared, "other-owner-device-91bc", campsites),
        )
        assertEquals(
            listOf(LotteryCampsite(lakeLouise.id, lakeLouise.name)),
            SharedLotteryController.campsitesOwnedIn(shared, "owner-device-7f3a", campsites),
        )
    }

    @Test
    fun sharedLotteriesFor_closedLottery_onlyShownIfYouAreInIt() {
        val shared = SharedLotteryController.addCampsite(lottery, waterton, "other-owner-device-91bc", now)
        val later = now + 3 * day

        // verify after registration closes, only owners with a campsite in it still see it
        assertEquals(listOf(shared), SharedLotteryController.sharedLotteriesFor(listOf(shared), "other-owner-device-91bc", campsites, later))
        assertEquals(emptyList<Lottery>(), SharedLotteryController.sharedLotteriesFor(listOf(shared), "third-owner-device-44aa", campsites, later))
    }

    @Test
    fun editLottery_creatorKeepsOtherOwnersCampsite_saved() {
        val shared = SharedLotteryController.addCampsite(lottery, waterton, "other-owner-device-91bc", now)

        // the creator edits the winner count, keeping the other owner's campsite in the lottery
        val edited = LotteryUpdateController.prepareUpdate(
            shared, "owner-device-7f3a", listOf(lakeLouise, waterton), shared.copy(winnerCount = 2), now,
        )

        // verify the other owner's campsite is still there
        assertTrue(LotteryController.includesCampsite(edited, waterton.id))
        assertEquals(2, edited.winnerCount)
    }

    @Test
    fun editLottery_creatorAddsOtherOwnersCampsite_refused() {
        // verify the creator can't pull in another owner's campsite that wasn't added by them
        assertThrows(IllegalArgumentException::class.java) {
            LotteryUpdateController.prepareUpdate(lottery, "owner-device-7f3a", listOf(lakeLouise, waterton), lottery, now)
        }
    }
}
