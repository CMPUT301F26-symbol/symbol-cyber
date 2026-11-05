/*
 * DrawResult.kt
 * Model of the result of one lottery draw: who won and who was not
 * selected. Data only; the draw itself is in DrawController.
 * Known issues: none.
 */

package com.example.code.model

/**
 * Winners and entrants not selected in one draw.
 *
 * @property winners entrants drawn.
 * @property notSelected everyone else on the waiting list.
 */
data class DrawResult(
    val winners: List<WaitingListEntry>,
    val notSelected: List<WaitingListEntry>,
)
