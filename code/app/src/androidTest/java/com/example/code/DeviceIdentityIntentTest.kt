/*
 * DeviceIdentityIntentTest.kt
 * Instrumented tests for US 01.09.01 device-based identification.
 * Verifies that Android provides a usable and stable device identifier.
 */

package com.example.code

import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceIdentityIntentTest {

    @Test
    fun deviceProvidesNonBlankIdentifier() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val deviceId =
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID,
            )

        assertTrue(deviceId.isNotBlank())
    }

    @Test
    fun deviceIdentifierRemainsStable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val firstDeviceId =
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID,
            )

        val secondDeviceId =
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID,
            )

        assertEquals(firstDeviceId, secondDeviceId)
    }
}