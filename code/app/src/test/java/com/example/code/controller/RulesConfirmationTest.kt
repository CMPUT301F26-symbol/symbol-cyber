/*
 * RulesConfirmationTest.kt
 * Unit tests for confirming the campsite rules and policies before
 * reserving (US 02.11.01).
 * AI use: written with Claude (Anthropic), Nov 2026; reviewed by Aalok Mishra.
 * Known issues: none.
 */

package com.example.code.controller

import com.example.code.fakes.FakeReservationRepository
import com.example.code.fakes.TestData
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesConfirmationTest {

    private val repository = FakeReservationRepository()
    private val controller = ReservationController(repository) { TestData.AFTER_REGISTRATION }
    private val drawnLottery = TestData.jasperLottery.copy(status = Lottery.STATUS_DRAWN)
    private val priyaWon = TestData.priya.copy(status = WaitingListEntry.STATUS_WON)

    @Test
    fun winnerMustConfirmRulesWhenTheCampsiteHasThem() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { controller.acceptWin(drawnLottery, priyaWon, rulesRequired = true, rulesAccepted = false) }
        }
        assertEquals(ReservationController.RULES_NOT_CONFIRMED, error.message)
        assertTrue(repository.reservations.value.isEmpty())
    }

    @Test
    fun confirmedRulesAreSavedWithTheReservation() = runBlocking {
        val reservation = controller.acceptWin(drawnLottery, priyaWon, rulesRequired = true, rulesAccepted = true)

        assertTrue(reservation!!.rulesAccepted)
        assertTrue(repository.reservations.value.getValue(reservation.id).rulesAccepted)
    }

    @Test
    fun campsiteWithoutRulesNeedsNoConfirmation() = runBlocking {
        val reservation = controller.acceptWin(drawnLottery, priyaWon)

        assertFalse(reservation!!.rulesAccepted)
    }

    @Test
    fun rulesAreRequiredWhenRulesOrPolicyExist() {
        assertTrue(ReservationController.rulesRequired(Campsite(rules = "No fires after 11 pm")))
        assertTrue(ReservationController.rulesRequired(Campsite(cancellationPolicy = "Free until 7 days before")))
        assertFalse(ReservationController.rulesRequired(Campsite(rules = " ")))
        assertFalse(ReservationController.rulesRequired(null))
    }
}
