// Rules for shared lotteries, where owners add their own campsites to a lottery for the same stay. Known issue is that only the owner who created the lottery sees its waiting list.
// AI use: Claude (Anthropic) helped me understand this and assisted in a bit of writing.
package com.example.code.controller

import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.LotteryCampsite

/**
 * Rules for adding and removing campsites in a lottery shared by several owners.
 *
 * Every owner can only add or remove the campsites they own.
 */
object SharedLotteryController {

    /**
     * Checks if campsites can still be added to or removed from a lottery.
     *
     * Campsites stay fixed once entrants join, so nobody's entry changes under them.
     *
     * @param lottery the lottery to check
     * @param now the current time in milliseconds
     * @return true if the lottery is open, registration hasn't ended and nobody has joined yet
     */
    fun canChangeCampsites(lottery: Lottery, now: Long): Boolean {
        return LotteryUpdateController.canEdit(lottery, now) && lottery.waitingListCount == 0
    }

    /**
     * Finds who owns a campsite.
     *
     * @param campsiteId the id of the campsite
     * @param campsites every campsite in the app
     * @return the owner's device id, or null if the campsite no longer exists
     */
    fun ownerOf(campsiteId: String, campsites: List<Campsite>): String? {
        return campsites.firstOrNull { it.id == campsiteId }?.ownerId
    }

    /**
     * Returns the campsites in a lottery that belong to one owner.
     *
     * @param lottery the lottery to look in
     * @param ownerId the owner's device id
     * @param campsites every campsite in the app, used to find each campsite's owner
     * @return that owner's campsites in the lottery
     */
    fun campsitesOwnedIn(lottery: Lottery, ownerId: String, campsites: List<Campsite>): List<LotteryCampsite> {
        return LotteryController.includedCampsites(lottery).filter { ownerOf(it.id, campsites) == ownerId }
    }

    /**
     * Returns the lotteries an owner can see on the shared lotteries screen, latest first.
     *
     * That is every lottery still open for campsites, plus any lottery that already has one of their campsites.
     *
     * @param lotteries every lottery in the app
     * @param ownerId the owner's device id
     * @param campsites every campsite in the app
     * @param now the current time in milliseconds
     * @return the lotteries to show
     */
    fun sharedLotteriesFor(
        lotteries: List<Lottery>,
        ownerId: String,
        campsites: List<Campsite>,
        now: Long,
    ): List<Lottery> {
        return lotteries
            .filter { canChangeCampsites(it, now) || campsitesOwnedIn(it, ownerId, campsites).isNotEmpty() }
            .sortedByDescending { it.registrationEnd }
    }

    /**
     * Adds one of the owner's campsites to a lottery.
     *
     * @param lottery the lottery to add to
     * @param campsite the campsite to add
     * @param ownerId the device id of the owner adding it
     * @param now the current time in milliseconds
     * @return the lottery with the campsite added
     * @throws IllegalArgumentException with a message for the owner if it can't be added
     */
    fun addCampsite(lottery: Lottery, campsite: Campsite, ownerId: String, now: Long): Lottery {
        require(campsite.ownerId == ownerId) { "You can only add your own campsites." }
        require(canChangeCampsites(lottery, now)) {
            "Campsites can't change once entrants join or registration closes."
        }
        require(!LotteryController.includesCampsite(lottery, campsite.id)) {
            "This campsite is already in the lottery."
        }

        val added = LotteryController.includedCampsites(lottery) + LotteryCampsite(campsite.id, campsite.name)
        return withCampsites(lottery, added)
    }

    /**
     * Removes one of the owner's campsites from a lottery.
     *
     * @param lottery the lottery to remove from
     * @param campsite the campsite to remove
     * @param ownerId the device id of the owner removing it
     * @param now the current time in milliseconds
     * @return the lottery without the campsite
     * @throws IllegalArgumentException with a message for the owner if it can't be removed
     */
    fun removeCampsite(lottery: Lottery, campsite: Campsite, ownerId: String, now: Long): Lottery {
        require(campsite.ownerId == ownerId) { "You can only remove your own campsites." }
        require(canChangeCampsites(lottery, now)) {
            "Campsites can't change once entrants join or registration closes."
        }
        require(LotteryController.includesCampsite(lottery, campsite.id)) {
            "This campsite isn't in the lottery."
        }

        val remaining = LotteryController.includedCampsites(lottery).filter { it.id != campsite.id }

        // A lottery always needs something to win
        require(remaining.isNotEmpty()) { "A lottery needs at least one campsite." }
        return withCampsites(lottery, remaining)
    }

    /**
     * Puts a new list of campsites into a lottery the same way the team saves them.
     *
     * The first campsite is also kept in campsiteId and campsiteName for older screens.
     *
     * @param lottery the lottery to change
     * @param campsites the campsites it should now include
     * @return the changed lottery
     */
    private fun withCampsites(lottery: Lottery, campsites: List<LotteryCampsite>): Lottery {
        return lottery.copy(
            campsiteId = campsites.first().id,
            campsiteName = campsites.first().name,
            campsites = if (campsites.size > 1) campsites else emptyList(),
        )
    }
}
