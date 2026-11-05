/*
 * MainActivity.kt
 * Single activity of the app. Hosts the Compose UI and the navigation shell.
 * Device identity is shared by owner and lottery flows inside the navigation shell.
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
