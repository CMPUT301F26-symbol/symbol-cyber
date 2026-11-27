/*
 * OwnerCancelledReservations.kt
 * Finds the cancelled reservations for a campsite owner's campsites (US 02.07.03).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.model.Reservation

/**
 * Cancelled reservations for campsites of owner [ownerId]: cancelled by the
 * entrant or rejected by the owner, newest stay first (US 02.07.03).
 */
fun cancelledReservationsForOwner(all: List<Reservation>, ownerId: String): List<Reservation> =
    all.filter {
        it.ownerId == ownerId &&
            (it.status == Reservation.STATUS_CANCELLED || it.status == Reservation.STATUS_REJECTED)
    }.sortedByDescending { it.startDate }
