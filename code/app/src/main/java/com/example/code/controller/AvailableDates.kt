/*
 * AvailableDates.kt
 * Works out which days of a lottery's booking period a winner can still
 * reserve on a campsite (US 01.04.01). Days are whole calendar days in the
 * phone's time zone, so the last day is offered even if the booking period
 * ends earlier in the day than it starts, and daylight saving changes don't
 * shift them.
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Abhishek Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.Lottery
import com.example.code.model.Reservation
import java.util.Calendar

/** One day in milliseconds (about; calendar days can be 23 or 25 hours long). */
const val DAY_MILLIS = 86_400_000L

/** Shown when the chosen days are outside the booking period or already booked. */
const val DATES_NOT_AVAILABLE = "Those dates are not available. Please choose other dates"

/** Midnight at the start of the calendar day that contains [time]. */
fun startOfDay(time: Long): Long = Calendar.getInstance().apply {
    timeInMillis = time
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/** Midnight at the start of the calendar day after [day]. */
fun nextDay(day: Long): Long = Calendar.getInstance().apply {
    timeInMillis = startOfDay(day)
    add(Calendar.DAY_OF_MONTH, 1)
}.timeInMillis

/** The same time of day on the calendar day after [time]. */
fun sameTimeNextDay(time: Long): Long = Calendar.getInstance().apply {
    timeInMillis = time
    add(Calendar.DAY_OF_MONTH, 1)
}.timeInMillis

/** The last millisecond of the calendar day that contains [day]: when a stay ending that day ends. */
fun endOfDay(day: Long): Long = nextDay(day) - 1

/** Every calendar day of [lottery]'s booking period, as midnights, first day first. */
fun bookingDays(lottery: Lottery): List<Long> {
    val last = startOfDay(lottery.bookingEnd)
    return generateSequence(startOfDay(lottery.bookingStart)) { nextDay(it) }
        .takeWhile { it <= last }
        .toList()
}

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
    return bookingDays(lottery).filter { day ->
        booked.none { day <= it.endDate && endOfDay(day) >= it.startDate }
    }
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
    if (start > end) return false
    val first = startOfDay(start)
    val last = startOfDay(end)
    if (first < startOfDay(lottery.bookingStart) || last > startOfDay(lottery.bookingEnd)) return false
    val free = availableDays(lottery, all, entrantId, campsiteId).toSet()
    return generateSequence(first) { nextDay(it) }.takeWhile { it <= last }.all { it in free }
}

/** Last days the winner can pick after arriving on [start]: the free days that follow without a gap. */
fun endDayOptions(available: List<Long>, start: Long): List<Long> {
    val free = available.toSet()
    return generateSequence(start) { sameTimeNextDay(it) }.takeWhile { it in free }.toList()
}
