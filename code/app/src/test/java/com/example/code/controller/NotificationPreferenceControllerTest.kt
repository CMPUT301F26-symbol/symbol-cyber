/*
 * NotificationPreferenceControllerTest.kt
 * Tests US 01.10.01 persistence, recovery from save failures, and profile preservation.
 */
package com.example.code.controller

import com.example.code.User
import com.example.code.UserRole
import com.example.code.data.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NotificationPreferenceControllerTest {
    private class Repository : UserRepository {
        var stored = User(
            deviceId = "entrant-23",
            name = "Amr",
            email = "amr@example.com",
            phoneNumber = "7805550100",
            profilePhotoUrl = "https://example.com/profile.jpg",
            roles = setOf(UserRole.ENTRANT, UserRole.CAMPSITE_OWNER),
        )
        var failSave = false
        override suspend fun getUser(deviceId: String): User = stored
        override suspend fun createUser(deviceId: String): User = error("Unexpected creation")
        override suspend fun updateUser(user: User) {
            if (failSave) error("Test save failure")
            stored = user
        }
    }

    @Test
    fun optOutPersistsAndSurvivesReloadThenOptInResumes() = runBlocking {
        val repository = Repository()
        val controller = UserController(repository)
        controller.loadOrCreateUser("entrant-23")
        assertTrue(controller.updateNotificationPreference(false))
        assertFalse(repository.stored.notificationsEnabled)

        val reopened = UserController(repository)
        reopened.loadOrCreateUser("entrant-23")
        assertFalse(reopened.currentUser.value!!.notificationsEnabled)
        assertTrue(reopened.updateNotificationPreference(true))
        assertTrue(repository.stored.notificationsEnabled)
    }

    @Test
    fun preferenceSavePreservesProfileAndRoles() = runBlocking {
        val repository = Repository()
        val before = repository.stored
        val controller = UserController(repository)
        controller.loadOrCreateUser("entrant-23")
        assertTrue(controller.updateNotificationPreference(false))
        val saved = repository.stored
        assertEquals(before.deviceId, saved.deviceId)
        assertEquals(before.name, saved.name)
        assertEquals(before.email, saved.email)
        assertEquals(before.phoneNumber, saved.phoneNumber)
        assertEquals(before.profilePhotoUrl, saved.profilePhotoUrl)
        assertEquals(before.roles, saved.roles)
        assertTrue(before.notificationsEnabled)
    }

    @Test
    fun failedSaveKeepsPreviousSettingAndAllowsRetry() = runBlocking {
        val repository = Repository()
        val controller = UserController(repository)
        controller.loadOrCreateUser("entrant-23")
        val before = controller.currentUser.value
        repository.failSave = true
        assertFalse(controller.updateNotificationPreference(false))
        assertSame(before, controller.currentUser.value)
        assertTrue(repository.stored.notificationsEnabled)
        assertNotNull(controller.errorMessage.value)
        assertFalse(controller.isLoading.value)

        repository.failSave = false
        assertTrue(controller.updateNotificationPreference(false))
        assertNull(controller.errorMessage.value)
    }

    @Test
    fun editingProfileDoesNotResetOptOut() = runBlocking {
        val repository = Repository()
        val controller = UserController(repository)
        controller.loadOrCreateUser("entrant-23")
        assertTrue(controller.updateNotificationPreference(false))
        assertTrue(controller.updateProfile("Updated", "updated@example.com", "7805550200"))
        assertFalse(repository.stored.notificationsEnabled)
        assertEquals("Updated", repository.stored.name)
    }

    @Test
    fun newProfileWithBlankEmailCanStillOptOut() = runBlocking {
        val repository = Repository()
        repository.stored.email = ""
        val controller = UserController(repository)
        controller.loadOrCreateUser("entrant-23")
        assertTrue(controller.updateNotificationPreference(false))
        assertEquals("", repository.stored.email)
    }
}
