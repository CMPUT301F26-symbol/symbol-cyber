/*
 * UserControllerTest.kt
 * Unit tests for device-based user loading and creation.
 */

package com.example.code.controller

import com.example.code.User
import com.example.code.UserRole
import com.example.code.data.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class UserControllerTest {

    @Test
    fun loadOrCreateUser_existingUser_loadsExistingProfile() = runBlocking {
        val existingUser =
            User(
                deviceId = "device-123",
                name = "Amr",
                email = "amr@example.com",
                phoneNumber = "7805550100",
                roles = setOf(UserRole.ENTRANT),
            )

        val repository =
            FakeUserRepository(existingUser = existingUser)

        val controller = UserController(repository)

        controller.loadOrCreateUser("device-123")

        assertSame(existingUser, controller.currentUser.value)
        assertEquals(0, repository.createCalls)
        assertFalse(controller.isLoading.value)
        assertNull(controller.errorMessage.value)
    }

    @Test
    fun loadOrCreateUser_newDevice_createsProfile() = runBlocking {
        val repository = FakeUserRepository()

        val controller = UserController(repository)

        controller.loadOrCreateUser("new-device")

        assertEquals("new-device", controller.currentUser.value?.deviceId)
        assertEquals(
            setOf(UserRole.ENTRANT),
            controller.currentUser.value?.roles,
        )
        assertEquals(1, repository.createCalls)
        assertFalse(controller.isLoading.value)
        assertNull(controller.errorMessage.value)
    }

    @Test
    fun loadOrCreateUser_blankDeviceId_returnsError() = runBlocking {
        val repository = FakeUserRepository()

        val controller = UserController(repository)

        controller.loadOrCreateUser("")

        assertNull(controller.currentUser.value)
        assertEquals(
            "Device identity is unavailable.",
            controller.errorMessage.value,
        )
        assertEquals(0, repository.createCalls)
    }

    private class FakeUserRepository(
        private val existingUser: User? = null,
    ) : UserRepository {

        var createCalls = 0

        override suspend fun getUser(deviceId: String): User? {
            return existingUser
        }

        override suspend fun createUser(deviceId: String): User {
            createCalls++

            return User(
                deviceId = deviceId,
                roles = setOf(UserRole.ENTRANT),
            )
        }
    }
}