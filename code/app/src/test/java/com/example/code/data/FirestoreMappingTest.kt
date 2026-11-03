/*
 * FirestoreMappingTest.kt
 * Unit tests for converting lotteries and waiting list entries to and from
 * Firestore fields.
 * Known issues: none.
 */

package com.example.code.data

import com.example.code.fakes.TestData
import com.example.code.model.Lottery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreMappingTest {

    @Test
    fun lotteryIsReadFromAllFields() {
        val data = mapOf<String, Any?>(
            "campsiteId" to "camp-jasper-riverside",
            "campsiteName" to "Jasper Riverside",
            "ownerId" to TestData.OWNER_ID,
            "registrationStart" to TestData.REGISTRATION_START,
            "registrationEnd" to TestData.REGISTRATION_END,
            "bookingStart" to TestData.BOOKING_START,
            "bookingEnd" to TestData.BOOKING_END,
            "winnerCount" to 2L,
            "waitlistCapacity" to 4L,
            "waitingListCount" to 0L,
            "status" to "OPEN",
        )
        assertEquals(TestData.jasperLottery, lotteryFromMap("lottery-jasper-nov", data))
    }

    @Test
    fun missingLotteryFieldsUseDefaults() {
        val lottery = lotteryFromMap("x", mapOf("campsiteName" to "Elk Island"))
        assertEquals("Elk Island", lottery.campsiteName)
        assertEquals(1, lottery.winnerCount)
        assertNull(lottery.waitlistCapacity)
        assertEquals(Lottery.STATUS_OPEN, lottery.status)
    }

    @Test
    fun waitingListEntryRoundTrips() {
        val entry = TestData.sofia
        assertEquals(entry, entryFromMap(entry.entrantId, entryToMap(entry)))
    }
}
