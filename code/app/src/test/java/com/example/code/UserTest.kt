/*
 * UserTest.kt
 * Unit tests for User profile updates, notification preferences, and roles.
 */

package com.example.code

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTest {

    @Test
    fun updateProfile_changesPersonalInformation() {
        val user =
            User(
                deviceId = "device-123",
                name = "Old Name",
                email = "old@example.com",
                phoneNumber = "7805550000",
                profilePhotoUrl = "old-photo",
                roles = setOf(UserRole.ENTRANT),
            )

        user.updateProfile(
            name = "Amr Soliman",
            email = "amr@example.com",
            phoneNumber = "7805551234",
            profilePhotoUrl = "new-photo",
        )

        assertEquals("Amr Soliman", user.name)
        assertEquals("amr@example.com", user.email)
        assertEquals("7805551234", user.phoneNumber)
        assertEquals("new-photo", user.profilePhotoUrl)
    }

    @Test
    fun updateProfile_withoutNewPhoto_keepsExistingPhoto() {
        val user =
            User(
                deviceId = "device-123",
                profilePhotoUrl = "existing-photo",
                roles = setOf(UserRole.ENTRANT),
            )

        user.updateProfile(
            name = "Amr",
            email = "amr@example.com",
            phoneNumber = "7805551234",
        )

        assertEquals("existing-photo", user.profilePhotoUrl)
    }

    @Test
    fun updateNotificationPreference_changesPreference() {
        val user =
            User(
                deviceId = "device-123",
                notificationsEnabled = true,
                roles = setOf(UserRole.ENTRANT),
            )

        user.updateNotificationPreference(false)

        assertFalse(user.notificationsEnabled)

        user.updateNotificationPreference(true)

        assertTrue(user.notificationsEnabled)
    }

    @Test
    fun canUseRole_returnsTrueForAssignedRole() {
        val user =
            User(
                deviceId = "device-123",
                roles = setOf(
                    UserRole.ENTRANT,
                    UserRole.CAMPSITE_OWNER,
                ),
            )

        assertTrue(user.canUseRole(UserRole.ENTRANT))
        assertTrue(user.canUseRole(UserRole.CAMPSITE_OWNER))
    }

    @Test
    fun canUseRole_returnsFalseForUnassignedRole() {
        val user =
            User(
                deviceId = "device-123",
                roles = setOf(UserRole.ENTRANT),
            )

        assertFalse(user.canUseRole(UserRole.CAMPSITE_OWNER))
        assertFalse(user.canUseRole(UserRole.ADMIN))
    }

    @Test
    fun adminWithoutOtherRoles_cannotUseEntrantOrOwnerRole() {
        val user =
            User(
                deviceId = "device-admin",
                roles = setOf(UserRole.ADMIN),
            )

        assertTrue(user.canUseRole(UserRole.ADMIN))
        assertFalse(user.canUseRole(UserRole.ENTRANT))
        assertFalse(user.canUseRole(UserRole.CAMPSITE_OWNER))
    }

    @Test
    fun adminWithMultipleRoles_canUseAllAssignedRoles() {
        val user =
            User(
                deviceId = "device-admin",
                roles =
                    setOf(
                        UserRole.ADMIN,
                        UserRole.ENTRANT,
                        UserRole.CAMPSITE_OWNER,
                    ),
            )

        assertTrue(user.canUseRole(UserRole.ADMIN))
        assertTrue(user.canUseRole(UserRole.ENTRANT))
        assertTrue(user.canUseRole(UserRole.CAMPSITE_OWNER))
    }
}