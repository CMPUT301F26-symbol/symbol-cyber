/*
 * DestinationTest.kt
 * JUnit tests for Destination: tabs per mode match the UI mockups
 * (E02, O01, A01) and routes are unique.
 * Known issues: none.
 */
package com.example.code.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationTest {

    @Test
    fun entrantTabsMatchMockup() {
        val labels = Destination.tabsFor(AppMode.ENTRANT).map { it.label }
        assertEquals(listOf("Explore", "Scan", "Reservations", "Notifications", "Profile"), labels)
    }

    @Test
    fun ownerTabsMatchMockup() {
        val labels = Destination.tabsFor(AppMode.OWNER).map { it.label }
        assertEquals(listOf("Campsites", "Lotteries", "Reservations", "Profile"), labels)
    }

    @Test
    fun adminTabsMatchMockup() {
        val labels = Destination.tabsFor(AppMode.ADMIN).map { it.label }
        assertEquals(listOf("Campsites", "Profiles", "Images", "Logs"), labels)
    }

    @Test
    fun adminProfileIsNotATab() {
        assertFalse(Destination.ADMIN_PROFILE in Destination.tabsFor(AppMode.ADMIN))
    }

    @Test
    fun startScreenIsFirstTab() {
        assertEquals(Destination.ENTRANT_EXPLORE, Destination.startFor(AppMode.ENTRANT))
        assertEquals(Destination.OWNER_CAMPSITES, Destination.startFor(AppMode.OWNER))
        assertEquals(Destination.ADMIN_CAMPSITES, Destination.startFor(AppMode.ADMIN))
    }

    @Test
    fun everyModeHasAProfileInThatMode() {
        AppMode.entries.forEach { mode ->
            assertEquals(mode, Destination.profileFor(mode).mode)
        }
    }

    @Test
    fun routesAreUniqueAndStartWithMode() {
        val routes = Destination.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
        Destination.entries.forEach {
            assertTrue(it.route.startsWith(it.mode.name.lowercase() + "/"))
        }
    }

    @Test
    fun fromRouteFindsDestination() {
        assertEquals(Destination.OWNER_LOTTERIES, Destination.fromRoute("owner/lotteries"))
        assertNull(Destination.fromRoute("owner/unknown"))
        assertNull(Destination.fromRoute(null))
    }
}
