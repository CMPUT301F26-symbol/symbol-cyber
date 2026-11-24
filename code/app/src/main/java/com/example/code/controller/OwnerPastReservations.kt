/*
 * OwnerPastReservations.kt
 * Finds the past reservations for a campsite owner's campsites (US 02.07.02).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.Reservation

/**
 * Past reservations at [now] for campsites of owner [ownerId]: reservations
 * that stood and whose stay ended before [now], most recent first (US 02.07.02).
 */
fun pastReservationsForOwner(
    all: List<Reservation>,
    ownerId: String,
    now: Long,
): List<Reservation> =
    all.filter {
        it.ownerId == ownerId && it.status == Reservation.STATUS_ACTIVE && it.endDate < now
    }.sortedByDescending { it.endDate }
