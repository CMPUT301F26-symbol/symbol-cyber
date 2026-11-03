/*
 * MainActivity.kt
 * Single activity of the app. Hosts the Compose UI and the navigation shell.
 * Supplies the device identifier for owner detail navigation; profile and role
 * persistence are handled by the identity stories.
 */
package com.example.code

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.code.navigation.AppNavHost
import com.example.code.ui.theme.CodeTheme

/** Entry point of the app. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val ownerDeviceId = Settings.Secure.getString(
            contentResolver, Settings.Secure.ANDROID_ID,
        ).orEmpty()
        setContent {
            CodeTheme {
                AppNavHost(ownerId = ownerDeviceId)
            }
        }
    }
}
