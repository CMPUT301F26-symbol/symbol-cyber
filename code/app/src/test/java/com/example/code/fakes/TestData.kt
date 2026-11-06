/*
 * TestData.kt
 * Realistic sample lotteries, entrants and times shared by the unit tests.
 * Known issues: none.
 */

package com.example.code.fakes

import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.WaitingListEntry

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

    /** Nov 20: first night of a later stay. */
    const val LATER_START = 1795154400000L

    /** Nov 22: last day of a later stay. */
    const val LATER_END = 1795327200000L

    /** Entrants on a waiting list. */
    val priya = WaitingListEntry("device-priya-01", "Priya Shah", REGISTRATION_START + 60_000)
    val liam = WaitingListEntry("device-liam-02", "Liam Chen", REGISTRATION_START + 120_000)
    val sofia = WaitingListEntry("device-sofia-03", "Sofia Martins", REGISTRATION_START + 180_000)
    val noah = WaitingListEntry("device-noah-04", "Noah Tremblay", REGISTRATION_START + 240_000)

    /** Priya's active reservation at Jasper Riverside, Nov 14-16. */
    val priyaReservation = Reservation(
        id = "lottery-jasper-nov_device-priya-01",
        lotteryId = "lottery-jasper-nov",
        campsiteId = "camp-jasper-riverside",
        campsiteName = "Jasper Riverside",
        entrantId = "device-priya-01",
        entrantName = "Priya Shah",
        ownerId = OWNER_ID,
        startDate = BOOKING_START,
        endDate = BOOKING_END,
        createdAt = AFTER_REGISTRATION,
    )

    /** Liam's active reservation at Banff Tunnel Mountain, Nov 20-22. */
    val liamReservation = priyaReservation.copy(
        id = "lottery-banff-nov_device-liam-02",
        lotteryId = "lottery-banff-nov",
        campsiteId = "camp-banff-tunnel",
        campsiteName = "Banff Tunnel Mountain",
        entrantId = "device-liam-02",
        entrantName = "Liam Chen",
        startDate = LATER_START,
        endDate = LATER_END,
    )
}
