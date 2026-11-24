/*
 * EntrantPastReservations.kt
 * Finds an entrant's past reservations (US 01.06.02).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.Reservation

/**
 * Entrant [entrantId]'s past reservations at [now]: reservations that stood
 * and whose stay ended before [now], most recent first (US 01.06.02).
 */
fun pastReservationsForEntrant(
    all: List<Reservation>,
    entrantId: String,
    now: Long,
): List<Reservation> =
    all.filter {
        it.entrantId == entrantId && it.status == Reservation.STATUS_ACTIVE && it.endDate < now
    }.sortedByDescending { it.endDate }
