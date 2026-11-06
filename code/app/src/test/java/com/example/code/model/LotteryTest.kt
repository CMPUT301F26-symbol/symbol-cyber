package com.example.code.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test


/** AI USAGE
 * The basis for the following test bench for the Lottery Creation file was generated using
 * GPT6-Sol "As part of my project requirements, I need develop unit tests for my files. Please
 * create tests for the new LotteryCreation file", 11-05-2026
 *
 */


/**
 * Local JUnit tests for the Lottery model only.
 *
 * Tests the data contract used by creation, waiting-list, and draw code.
 * No controller, repository, Firebase instance, Android device, or emulator is used.
 * Period validation and winner selection belong in their controller tests.
 */
class LotteryTest {

    // Realistic sample: registration in April, followed by a May weekend stay.
    // Explicit UTC timestamps make these tests independent of the computer's zone.
    // The corresponding Alberta times are April 1 at 09:00, April 8 at 17:00,
    // May 14 at 15:00, and May 16 at 11:00 (Mountain Daylight Time).
    private val registrationStart = Instant.parse("2027-04-01T15:00:00Z").toEpochMilli()
    private val registrationEnd = Instant.parse("2027-04-08T23:00:00Z").toEpochMilli()
    private val bookingStart = Instant.parse("2027-05-14T21:00:00Z").toEpochMilli()
    private val bookingEnd = Instant.parse("2027-05-16T17:00:00Z").toEpochMilli()

    private val lottery = Lottery(
        id = "lottery-pine-creek-may-2027",
        campsiteId = "campsite-pine-creek-17",
        campsiteName = "Pine Creek Campground",
        ownerId = "owner-device-pine-creek",
        registrationStart = registrationStart,
        registrationEnd = registrationEnd,
        bookingStart = bookingStart,
        bookingEnd = bookingEnd,
        winnerCount = 2,
        waitlistCapacity = 10,
    )

    // US 02.04.01 / US 02.04.07 / US 02.05.01:
    // A model created with defaults starts open, empty, and without a capacity.
    // These are data defaults, not proof that an unconfigured lottery is valid to publish.
    @Test
    fun newLotteryDefaultsToOpenEmptyAndUnlimited() {
        val emptyLottery = Lottery()

        assertEquals(Lottery.STATUS_OPEN, emptyLottery.status)
        assertEquals(0, emptyLottery.waitingListCount)
        assertNull(emptyLottery.waitlistCapacity)
        assertEquals(1, emptyLottery.winnerCount)
    }

    // US 02.04.01: The model holds its own identity and its campsite/owner link.
    @Test
    fun holdsLotteryCampsiteAndOwnerIdentity() {
        assertEquals("lottery-pine-creek-may-2027", lottery.id)
        assertEquals("campsite-pine-creek-17", lottery.campsiteId)
        assertEquals("Pine Creek Campground", lottery.campsiteName)
        assertEquals("owner-device-pine-creek", lottery.ownerId)
    }

    // US 02.04.03: Registration boundaries are held as epoch-millisecond values.
    @Test
    fun holdsAssignedRegistrationPeriod() {
        assertEquals(registrationStart, lottery.registrationStart)
        assertEquals(registrationEnd, lottery.registrationEnd)
    }

    // US 02.04.04: The stay period has separate fields from registration.
    // Booking dates represent the campsite stay, not an acceptance deadline.
    @Test
    fun holdsAssignedBookingPeriodSeparatelyFromRegistration() {
        assertEquals(bookingStart, lottery.bookingStart)
        assertEquals(bookingEnd, lottery.bookingEnd)
        assertEquals(registrationStart, lottery.registrationStart)
        assertEquals(registrationEnd, lottery.registrationEnd)
    }

    // US 02.04.05 / US 02.04.07:
    // Winner count and waiting-list capacity are separate settings.
    // Ten entrants may join even though only two are intended to win.
    @Test
    fun holdsWinnerCountAndOptionalCapacityIndependently() {
        assertEquals(2, lottery.winnerCount)
        assertEquals(10, lottery.waitlistCapacity)
    }

    // US 02.04.07: Null represents unlimited capacity while preserving N.
    @Test
    fun unlimitedCapacityDoesNotChangeWinnerCount() {
        val unlimitedLottery = lottery.copy(waitlistCapacity = null)

        assertNull(unlimitedLottery.waitlistCapacity)
        assertEquals(2, unlimitedLottery.winnerCount)

        // The immutable original remains capped.
        assertEquals(10, lottery.waitlistCapacity)
    }

    // US 02.04.01 / US 02.04.03 / US 02.04.04 / US 02.04.05 / US 02.04.07:
    // Waiting-list code uses copy() to produce a snapshot with an updated count.
    // Verify that this preserves the lottery's identity and owner settings.
    @Test
    fun updatedWaitingListSnapshotPreservesLotteryConfiguration() {
        val updated = lottery.copy(waitingListCount = 3)

        assertEquals(3, updated.waitingListCount)
        assertEquals(lottery.id, updated.id)
        assertEquals(lottery.campsiteId, updated.campsiteId)
        assertEquals(lottery.campsiteName, updated.campsiteName)
        assertEquals(lottery.ownerId, updated.ownerId)
        assertEquals(registrationStart, updated.registrationStart)
        assertEquals(registrationEnd, updated.registrationEnd)
        assertEquals(bookingStart, updated.bookingStart)
        assertEquals(bookingEnd, updated.bookingEnd)
        assertEquals(2, updated.winnerCount)
        assertEquals(10, updated.waitlistCapacity)
        assertEquals(Lottery.STATUS_OPEN, updated.status)

        // Creating the updated snapshot must not change the original model.
        assertEquals(0, lottery.waitingListCount)
    }
}