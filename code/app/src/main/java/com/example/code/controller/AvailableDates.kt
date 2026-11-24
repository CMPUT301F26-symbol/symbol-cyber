/*
 * AvailableDates.kt
 * Works out which days of a lottery's booking period a winner can still
 * reserve on a campsite (US 01.04.01).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.Lottery
import com.example.code.model.Reservation

/** One day in milliseconds. */
const val DAY_MILLIS = 86_400_000L

/** Shown when the chosen days are outside the booking period or already booked. */
const val DATES_NOT_AVAILABLE = "Those dates are not available. Please choose other dates"

/** Every day of [lottery]'s booking period, first day first. */
fun bookingDays(lottery: Lottery): List<Long> =
    generateSequence(lottery.bookingStart) { it + DAY_MILLIS }
        .takeWhile { it <= lottery.bookingEnd }
        .toList()

/**
 * Days of [lottery]'s booking period that no one else has booked on
 * [campsiteId] (US 01.04.01).
 *
 * @param entrantId the winner; their own reservation does not block days.
 */
fun availableDays(lottery: Lottery, all: List<Reservation>, entrantId: String, campsiteId: String): List<Long> {
    val booked = all.filter {
        it.campsiteId == campsiteId && it.entrantId != entrantId && it.status == Reservation.STATUS_ACTIVE
    }
    return bookingDays(lottery).filter { day -> booked.none { day >= it.startDate && day <= it.endDate } }
}

/** Returns true if every day from [start] to [end] is free for [entrantId] on [campsiteId]. */
fun isStayAvailable(
    lottery: Lottery,
    all: List<Reservation>,
    entrantId: String,
    campsiteId: String,
    start: Long,
    end: Long,
): Boolean {
    if (start > end || start < lottery.bookingStart || end > lottery.bookingEnd) return false
    val free = availableDays(lottery, all, entrantId, campsiteId).toSet()
    return generateSequence(start) { it + DAY_MILLIS }.takeWhile { it <= end }.all { it in free }
}

/** Last days the winner can pick after arriving on [start]: the free days that follow without a gap. */
fun endDayOptions(available: List<Long>, start: Long): List<Long> {
    val free = available.toSet()
    return generateSequence(start) { it + DAY_MILLIS }.takeWhile { it in free }.toList()
}
