/*
 * ModeControllerTest.kt
 * JUnit tests for ModeController: switching modes and blocking modes the
 * user does not have (US 03.11.01).
 * Known issues: none.
 */
package com.example.code.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeControllerTest {

    /** A campground owner who also books sites as an entrant (no admin role). */
    private fun ownerAndEntrant() = ModeController(setOf(AppMode.OWNER, AppMode.ENTRANT))

    @Test
    fun startsInEntrantModeByDefault() {
        val controller = ModeController()
        assertEquals(AppMode.ENTRANT, controller.activeMode.value)
        assertEquals("entrant/explore", controller.startRoute())
    }

    @Test
    fun availableModesAreInFixedOrder() {
        assertEquals(listOf(AppMode.ENTRANT, AppMode.OWNER), ownerAndEntrant().availableModes)
    }

    @Test
    fun switchToOwnerChangesTabsAndStart() {
        val controller = ownerAndEntrant()
        assertTrue(controller.switchTo(AppMode.OWNER))
        assertEquals(AppMode.OWNER, controller.activeMode.value)
        assertEquals("owner/campsites", controller.startRoute())
        assertEquals(Destination.tabsFor(AppMode.OWNER), controller.tabs())
    }

    @Test
    fun switchToSameModeReturnsFalse() {
        val controller = ownerAndEntrant()
        assertFalse(controller.switchTo(AppMode.ENTRANT))
        assertEquals(AppMode.ENTRANT, controller.activeMode.value)
    }

    @Test
    fun nonAdminCannotSwitchToAdmin() {
        val controller = ownerAndEntrant()
        assertFalse(controller.canSwitchTo(AppMode.ADMIN))
        assertThrows(IllegalArgumentException::class.java) {
            controller.switchTo(AppMode.ADMIN)
        }
        assertEquals(AppMode.ENTRANT, controller.activeMode.value)
    }

    @Test
    fun adminCanAlsoBeEntrantAndOwner() {
        val controller = ModeController(AppMode.entries.toSet(), AppMode.ADMIN)
        assertTrue(controller.switchTo(AppMode.ENTRANT))
        assertTrue(controller.switchTo(AppMode.OWNER))
        assertTrue(controller.switchTo(AppMode.ADMIN))
    }

    @Test
    fun emptyModesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ModeController(emptySet())
        }
    }

    @Test
    fun initialModeMustBeAvailable() {
        assertThrows(IllegalArgumentException::class.java) {
            ModeController(setOf(AppMode.ENTRANT), AppMode.ADMIN)
        }
    }
}
