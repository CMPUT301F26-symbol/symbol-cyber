/*
 * Dates.kt
 * Formats times stored as milliseconds since the epoch for display.
 * Known issues: always uses the device's time zone.
 */

package com.example.code.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formats [millis] as a date, e.g. "Nov 14, 2026". */
fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.CANADA).format(Date(millis))

/** Formats [millis] as a date and time, e.g. "Nov 14, 2026, 5:00 p.m.". */
fun formatDateTime(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.CANADA).format(Date(millis))

/** Formats a date range, e.g. "Nov 14, 2026 – Nov 16, 2026". */
fun formatDateRange(start: Long, end: Long): String =
    "${formatDate(start)} – ${formatDate(end)}"
