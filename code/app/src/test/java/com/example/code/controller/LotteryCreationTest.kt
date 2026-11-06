package com.example.code.controller
import com.example.code.data.LotteryCreationRepository
import com.example.code.data.newLotteryToMap
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.*
import org.junit.Test


/** AI USAGE
 * The basis for the following test bench for the Lottery Creation file was generated using
 * GPT6-Sol "As part of my project requirements, I need develop unit tests for my files. Please
 * create tests for the new LotteryCreation file", 11-05-2026
 *
 */
/** Boundary and integration-contract tests; no Firebase instance is required. */
class LotteryCreationTest {
    private val campsite = Campsite(id = "site-17", ownerId = "device-owner", name = "Pine Creek")
    private val valid = Lottery(
        campsiteId = campsite.id,
        campsiteName = campsite.name,
        ownerId = campsite.ownerId,
        registrationStart = 1_000L,
        registrationEnd = 2_000L,
        bookingStart = 3_000L,
        bookingEnd = 4_000L,
        winnerCount = 3,
    )

    @Test fun unlimitedCapacityIsValid() {
        LotteryController.validateCreation(valid)
    }

    @Test fun capacityMayEqualWinnerCount() {
        LotteryController.validateCreation(valid.copy(waitlistCapacity = 3))
    }

    @Test fun capacityMayExceedWinnerCount() {
        LotteryController.validateCreation(valid.copy(waitlistCapacity = 10))
    }

    @Test fun registrationMayEndExactlyWhenBookingStarts() {
        LotteryController.validateCreation(valid.copy(bookingStart = valid.registrationEnd))
    }

    @Test fun equalOrReversedRegistrationDatesAreRejected() {
        rejected { LotteryController.validateCreation(valid.copy(registrationEnd = 1_000L)) }
        rejected { LotteryController.validateCreation(valid.copy(registrationEnd = 999L)) }
    }

    @Test fun overlappingRegistrationAndBookingAreRejected() {
        rejected { LotteryController.validateCreation(valid.copy(bookingStart = 1_999L)) }
    }

    @Test fun equalOrReversedBookingDatesAreRejected() {
        rejected { LotteryController.validateCreation(valid.copy(bookingEnd = 3_000L)) }
        rejected { LotteryController.validateCreation(valid.copy(bookingEnd = 2_999L)) }
    }

    @Test fun zeroAndNegativeWinnersAreRejected() {
        listOf(0, -1).forEach { count ->
            rejected { LotteryController.validateCreation(valid.copy(winnerCount = count)) }
        }
    }

    @Test fun capacityBelowWinnersIsRejectedIncludingZeroAndNegative() {
        listOf(2, 0, -1).forEach { cap ->
            rejected { LotteryController.validateCreation(valid.copy(waitlistCapacity = cap)) }
        }
    }

    @Test fun missingCampsiteOrOwnerIsRejected() {
        listOf(
            valid.copy(campsiteId = ""), valid.copy(campsiteId = "bad/path"),
            valid.copy(campsiteName = " "), valid.copy(ownerId = ""),
        ).forEach { lottery -> rejected { LotteryController.validateCreation(lottery) } }
    }

    @Test fun publishUsesActualCampsiteIdentityAndResetsDrawState() {
        val fake = FakeCreator()
        val settings = valid.copy(
            id = "old-lottery", campsiteId = "spoofed", campsiteName = "Spoofed",
            ownerId = "another-owner", waitingListCount = 8, status = Lottery.STATUS_DRAWN,
        )
        val id = immediately { LotteryController.publishLottery(fake, campsite, campsite.ownerId, settings) }
        assertEquals("created-id", id)
        assertEquals(valid, fake.saved)
    }

    @Test fun anotherOwnerOrBlankIdentityCannotPublish() {
        val fake = FakeCreator()
        listOf("other-owner", "").forEach { owner ->
            rejected { immediately { LotteryController.publishLottery(fake, campsite, owner, valid) } }
        }
        assertEquals(0, fake.calls)
    }

    @Test fun invalidSettingsNeverReachPersistence() {
        val fake = FakeCreator()
        rejected {
            immediately { LotteryController.publishLottery(fake, campsite, campsite.ownerId, valid.copy(winnerCount = 0)) }
        }
        assertEquals(0, fake.calls)
    }

    @Test fun persistenceFailurePropagatesToTheForm() {
        val failure = IllegalStateException("Permission denied")
        val fake = object : LotteryCreationRepository {
            override suspend fun createLottery(lottery: Lottery): String = throw failure
        }
        try {
            immediately { LotteryController.publishLottery(fake, campsite, campsite.ownerId, valid) }
            fail("Expected the save to fail")
        } catch (actual: IllegalStateException) {
            assertSame(failure, actual)
        }
    }

    @Test fun unlimitedMapUsesExactSchemaAndNumericTypes() {
        val map = newLotteryToMap(valid)
        assertEquals(setOf(
            "campsiteId", "campsiteName", "ownerId", "registrationStart", "registrationEnd",
            "bookingStart", "bookingEnd", "winnerCount", "waitingListCount", "status",
        ), map.keys)
        assertEquals(campsite.id, map["campsiteId"])
        assertEquals(campsite.name, map["campsiteName"])
        assertEquals(campsite.ownerId, map["ownerId"])
        assertEquals(1_000L, map["registrationStart"])
        assertEquals(2_000L, map["registrationEnd"])
        assertEquals(3_000L, map["bookingStart"])
        assertEquals(4_000L, map["bookingEnd"])
        assertEquals(3, map["winnerCount"])
        assertEquals(0, map["waitingListCount"])
        assertEquals("OPEN", map["status"])
        assertFalse(map.containsKey("waitlistCapacity"))
        assertTrue(map["registrationStart"] is Long)
        assertTrue(map["winnerCount"] is Int)
    }

    @Test fun cappedMapIncludesCapacityAndAlwaysInitializesState() {
        val map = newLotteryToMap(valid.copy(
            waitlistCapacity = 10, waitingListCount = 99, status = Lottery.STATUS_DRAWN,
        ))
        assertEquals(10, map["waitlistCapacity"])
        assertTrue(map["waitlistCapacity"] is Int)
        assertEquals(0, map["waitingListCount"])
        assertEquals("OPEN", map["status"])
        assertFalse(map.containsKey("winnerIds"))
    }

    @Test fun existingRegistrationAndRoomRulesStillWork() {
        assertFalse(LotteryController.isRegistrationOpen(valid, 999L))
        assertTrue(LotteryController.isRegistrationOpen(valid, 1_000L))
        assertTrue(LotteryController.isRegistrationOpen(valid, 1_999L))
        assertFalse(LotteryController.isRegistrationOpen(valid, 2_000L))
        assertFalse(LotteryController.isRegistrationOpen(valid.copy(status = "DRAWN"), 1_500L))
        assertTrue(LotteryController.hasRoom(valid.copy(waitingListCount = 100)))
        assertTrue(LotteryController.hasRoom(valid.copy(waitlistCapacity = 3, waitingListCount = 2)))
        assertFalse(LotteryController.hasRoom(valid.copy(waitlistCapacity = 3, waitingListCount = 3)))
    }

    private class FakeCreator : LotteryCreationRepository {
        var saved: Lottery? = null
        var calls = 0
        override suspend fun createLottery(lottery: Lottery): String {
            calls++
            saved = lottery
            return "created-id"
        }
    }

    private fun rejected(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected validation failure.
        }
    }

    // The fake never suspends, so exercise suspend APIs without adding a
    // coroutine-test dependency to the team's existing test configuration.
    private fun <T> immediately(block: suspend () -> T): T {
        var completed: Result<T>? = null
        block.startCoroutine(object : Continuation<T> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) { completed = result }
        })
        return checkNotNull(completed) { "Fake unexpectedly suspended" }.getOrThrow()
    }
}
