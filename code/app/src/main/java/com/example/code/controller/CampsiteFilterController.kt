/*
 * CampsiteFilterController.kt
 * Validates browse input and resolves advertised dates against live lottery and reservation data.
 * Known issues: availability uses published lottery periods, the schedule available at this checkpoint.
 */
package com.example.code.controller

import com.example.code.model.Campsite
import com.example.code.model.CampsiteFilters
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.model.filterCampsites
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** A requested inclusive stay, in milliseconds since the epoch. */
data class FilterStay(val start: Long, val end: Long)

/**
 * Validated browse criteria.
 * @property location location text, ignoring case and surrounding whitespace.
 * @property guests minimum campsite capacity, or null for any capacity.
 * @property stay inclusive first/last stay dates, or null for any dates.
 */
data class CampsiteFilterSelection(
    val location: String = "",
    val guests: Int? = null,
    val stay: FilterStay? = null,
) {
    /** Whether any criterion is applied. */
    val isActive: Boolean get() = location.isNotBlank() || guests != null || stay != null
}

/** Applies the existing Campsite schema without changing the lottery/booking models. */
class CampsiteFilterController(private val zone: TimeZone = TimeZone.getDefault()) {
    /**
     * Parses optional location, guests and yyyy-MM-dd date fields.
     * @throws IllegalArgumentException when guests or dates are invalid/incomplete.
     */
    fun parse(location: String, guests: String, start: String, end: String): CampsiteFilterSelection {
        val count = guests.trim().takeIf { it.isNotEmpty() }?.let {
            require(it.matches(Regex("[0-9]+"))) { "Enter a whole number of guests." }
            requireNotNull(it.toIntOrNull()?.takeIf { value -> value > 0 }) { "Guests must be greater than zero." }
        }
        val first = start.trim()
        val last = end.trim()
        val stay = if (first.isEmpty() && last.isEmpty()) null else {
            require(first.isNotEmpty() && last.isNotEmpty()) { "Enter both stay dates." }
            val a = parseDate(first)
            val b = parseDate(last)
            require(a <= b) { "The end date must be on or after the start date." }
            FilterStay(a, b)
        }
        return CampsiteFilterSelection(location.trim(), count, stay)
    }

    /**
     * Returns sites satisfying every criterion using the current live records.
     * A stay must fit one advertised period and not overlap an active reservation.
     * Rejected reservations do not block dates. Stored stay endpoints are inclusive.
     */
    fun apply(
        campsites: List<Campsite>,
        selection: CampsiteFilterSelection,
        lotteries: List<Lottery>,
        reservations: List<Reservation>,
    ): List<Campsite> {
        val stay = selection.stay
        val availableIds = stay?.let {
            val start = day(it.start)
            val end = day(it.end)
            lotteries.filter { lottery ->
                lottery.bookingStart > 0 && lottery.bookingEnd >= lottery.bookingStart &&
                    day(lottery.bookingStart) <= start && day(lottery.bookingEnd) >= end &&
                    reservations.none { booking ->
                        booking.campsiteId == lottery.campsiteId &&
                            booking.status == Reservation.STATUS_ACTIVE &&
                            day(booking.startDate) <= end && day(booking.endDate) >= start
                    }
            }.map { lottery -> lottery.campsiteId }.toSet()
        }
        return filterCampsites(campsites, CampsiteFilters(selection.location, selection.guests, availableIds))
    }

    private fun parseDate(text: String): Long {
        require(text.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) { "Use YYYY-MM-DD for dates." }
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false; timeZone = zone }
        val position = ParsePosition(0)
        val parsed = format.parse(text, position)
        require(parsed != null && position.index == text.length) { "Enter a valid calendar date." }
        return parsed.time
    }

    private fun day(millis: Long): Int = Calendar.getInstance(zone).run {
        timeInMillis = millis
        get(Calendar.YEAR) * 10000 + (get(Calendar.MONTH) + 1) * 100 + get(Calendar.DAY_OF_MONTH)
    }
}
