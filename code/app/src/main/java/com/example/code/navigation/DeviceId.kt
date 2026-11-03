/*
 * DeviceId.kt
 * Gives screens this device's ID, used as the entrant's and owner's ID in
 * Firestore (waiting lists, lotteries).
 * Known issues: temporary until the User model (US 01.09.01) provides the
 * current user; then callers should use that user's deviceId instead.
 */

package com.example.code.navigation

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Returns this device's Android ID, remembered across recompositions. */
@SuppressLint("HardwareIds")
@Composable
fun rememberDeviceId(): String {
    val context = LocalContext.current
    return remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }
}
