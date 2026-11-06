/*
 * AutomaticDraw.kt
 * Runs the automatic lottery draw (US 02.05.01) while the app is open: when
 * the lottery list changes and then once a minute.
 * Known issues: a lottery is drawn only when some device has the app open
 * after its registration closes.
 */

package com.example.code.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.code.controller.DrawController
import com.example.code.model.Lottery
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** How often open lotteries are checked for a due draw. */
private const val DRAW_CHECK_INTERVAL_MS = 60_000L

/**
 * Draws due lotteries when [lotteries] changes and then once a minute.
 *
 * @param lotteries all lotteries.
 * @param controller draw controller.
 */
@Composable
fun AutomaticDraw(lotteries: List<Lottery>, controller: DrawController) {
    LaunchedEffect(lotteries) {
        while (isActive) {
            try {
                controller.drawAllDue(lotteries)
            } catch (e: Exception) {
                // A failed draw is retried on the next check.
            }
            delay(DRAW_CHECK_INTERVAL_MS)
        }
    }
}
