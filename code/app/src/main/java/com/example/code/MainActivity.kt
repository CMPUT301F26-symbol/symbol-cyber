/*
 * MainActivity.kt
 * Single activity of the app. Hosts the Compose UI and the navigation shell.
 * Known issues: no device check yet; the app opens in Entrant mode until the
 * User model (T3, US 01.09.01) is ready.
 */
package com.example.code

import android.os.Bundle
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
        setContent {
            CodeTheme {
                AppNavHost()
            }
        }
    }
}
