/*
 * TestData.kt
 * Realistic sample lotteries and times shared by the unit tests.
 * Known issues: none.
 */

package com.example.code.fakes

import com.example.code.model.Lottery

/** Sample data used across tests. Times are Mountain time (UTC-6), 2026. */
object TestData {
    /** Oct 31, 12:00: before registration opens. */
    const val BEFORE_REGISTRATION = 1793469600000L

    /** Nov 1, 09:00: registration opens. */
    const val REGISTRATION_START = 1793545200000L

    /** Nov 3, 12:00: during registration. */
    const val DURING_REGISTRATION = 1793728800000L

    /** Nov 8, 17:00: registration closes. */
    const val REGISTRATION_END = 1794178800000L

    /** Nov 9, 08:00: after registration has closed. */
    const val AFTER_REGISTRATION = 1794232800000L

    /** Nov 14: first night of the stay. */
    const val BOOKING_START = 1794636000000L

    /** Nov 16: last day of the stay. */
    const val BOOKING_END = 1794808800000L

    /** Device ID of the campsite owner. */
    const val OWNER_ID = "owner-device-7f3a"

    /** Lottery for Jasper Riverside: 2 winners, room for 4 entrants. */
    val jasperLottery = Lottery(
        id = "lottery-jasper-nov",
        campsiteId = "camp-jasper-riverside",
        campsiteName = "Jasper Riverside",
        ownerId = OWNER_ID,
        registrationStart = REGISTRATION_START,
        registrationEnd = REGISTRATION_END,
        bookingStart = BOOKING_START,
        bookingEnd = BOOKING_END,
        winnerCount = 2,
        waitlistCapacity = 4,
        waitingListCount = 0,
    )

    /** Lottery for Banff Tunnel Mountain with no waiting list cap. */
    val banffLottery = jasperLottery.copy(
        id = "lottery-banff-nov",
        campsiteId = "camp-banff-tunnel",
        campsiteName = "Banff Tunnel Mountain",
        winnerCount = 1,
        waitlistCapacity = null,
    )
}
