package com.example.code.ui.entrant

import java.util.Locale

/** A missing or invalid average is not presented as a real review score. */
fun campsiteRatingText(rating: Double?): String =
    if (rating != null && rating.isFinite() && rating in 1.0..5.0) {
        "Rating: ${String.format(Locale.getDefault(), "%.1f", rating)} / 5"
    } else {
        "No ratings yet"
    }
