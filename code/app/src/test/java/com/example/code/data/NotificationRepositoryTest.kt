/*
 * NotificationRepositoryTest.kt
 * Unit tests for notification delivery preference rules.
 */

package com.example.code.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRepositoryTest {

    @Test
    fun notificationsEnabled_deliversNotification() {
        assertTrue(
            shouldDeliverNotification(true),
        )
    }

    @Test
    fun notificationsDisabled_blocksNotification() {
        assertFalse(
            shouldDeliverNotification(false),
        )
    }

    @Test
    fun missingPreference_defaultsToEnabled() {
        assertTrue(
            shouldDeliverNotification(null),
        )
    }
}