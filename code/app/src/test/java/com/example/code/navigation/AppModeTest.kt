/*
 * AppModeTest.kt
 * Unit tests for converting stored user roles into available app modes.
 */

package com.example.code.navigation

import com.example.code.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class AppModeTest {

    @Test
    fun entrantRole_mapsToEntrantMode() {
        assertEquals(
            setOf(AppMode.ENTRANT),
            modesForRoles(
                setOf(UserRole.ENTRANT),
            ),
        )
    }

    @Test
    fun adminRoleAlone_mapsOnlyToAdminMode() {
        assertEquals(
            setOf(AppMode.ADMIN),
            modesForRoles(
                setOf(UserRole.ADMIN),
            ),
        )
    }

    @Test
    fun multipleRoles_mapToMultipleModes() {
        assertEquals(
            setOf(
                AppMode.ENTRANT,
                AppMode.OWNER,
                AppMode.ADMIN,
            ),
            modesForRoles(
                setOf(
                    UserRole.ENTRANT,
                    UserRole.CAMPSITE_OWNER,
                    UserRole.ADMIN,
                ),
            ),
        )
    }
}