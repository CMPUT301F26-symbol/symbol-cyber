/* Tests authorization, self-protection, live profile disappearance and failed removal. Known issues: none. */
package com.example.code.admin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AdminProfileControllerTest {
    private class FakeProfiles : AdminProfileRepository {
        val rows = MutableStateFlow(listOf(AdminProfile("device-avery", "Avery Chen", "avery@example.com")))
        var observations = 0
        var removals = 0
        var fail = false
        override fun observeProfiles(): Flow<List<AdminProfile>> { observations++; return rows }
        override suspend fun removeProfile(profileId: String): Boolean {
            removals++
            if (fail) error("offline")
            val exists = rows.value.any { it.id == profileId }
            rows.value = rows.value.filterNot { it.id == profileId }
            return exists
        }
    }

    @Test fun nonAdminsCannotBrowseOrDelete() = runBlocking {
        val repo = FakeProfiles()
        val controller = AdminProfileController(repo, { false }, "admin-device")
        val state = controller.observe().first { !it.loading }
        assertFalse(state.authorized)
        assertTrue(state.profiles.isEmpty())
        assertEquals(AdminRemovalResult.DENIED, controller.remove("device-avery"))
        assertEquals(0, repo.observations)
        assertEquals(0, repo.removals)
    }

    @Test fun confirmedRemovalDisappearsAndRepeatedRemovalIsSafe() = runBlocking {
        val repo = FakeProfiles()
        val controller = AdminProfileController(repo, { true }, "admin-device")
        assertEquals(1, controller.observe().first { !it.loading }.profiles.size)
        assertEquals(AdminRemovalResult.REMOVED, controller.remove("device-avery"))
        assertTrue(controller.observe().first { !it.loading }.profiles.isEmpty())
        assertEquals(AdminRemovalResult.ALREADY_REMOVED, controller.remove("device-avery"))
    }

    @Test fun selfAndMalformedIdsNeverReachStorage() = runBlocking {
        val repo = FakeProfiles()
        val controller = AdminProfileController(repo, { true }, "admin-device")
        for (id in listOf("", "admin-device", "users/other")) assertEquals(AdminRemovalResult.INVALID, controller.remove(id))
        assertEquals(0, repo.removals)
    }

    @Test fun revokedPermissionAndFailureLeaveTheProfileAvailable() = runBlocking {
        val repo = FakeProfiles()
        var allowed = true
        val controller = AdminProfileController(repo, { allowed }, "admin-device")
        controller.observe().first { !it.loading }
        allowed = false
        assertEquals(AdminRemovalResult.DENIED, controller.remove("device-avery"))
        allowed = true; repo.fail = true
        assertEquals(AdminRemovalResult.FAILED, controller.remove("device-avery"))
        assertEquals(1, repo.rows.value.size)
    }
}
