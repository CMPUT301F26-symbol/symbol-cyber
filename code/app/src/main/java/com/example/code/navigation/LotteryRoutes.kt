/*
 * LotteryRoutes.kt
 * Connects the lottery screens to WaitingListController.
 * Known issues: "now" is read when the screen is shown; the screen does not
 * refresh by itself when registration opens or closes.
 */

package com.example.code.navigation

import androidx.compose.runtime.Composable
import com.example.code.model.Lottery
import com.example.code.ui.entrant.LotterySection

/**
 * Lottery card on the entrant's campsite details screen.
 *
 * @param lottery the campsite's lottery, or null.
 */
@Composable
fun EntrantLotteryCard(lottery: Lottery?) {
    LotterySection(
        lottery = lottery,
        now = System.currentTimeMillis(),
    )
}
