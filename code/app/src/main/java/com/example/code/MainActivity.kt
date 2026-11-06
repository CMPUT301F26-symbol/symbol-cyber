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
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startApp()
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    startApp()
                }
                .addOnFailureListener {
                    Log.e("MainActivity", "Firebase login failed", it)
                }
        }
    }
    private fun startApp() {
        setContent {
            CodeTheme {
                AppNavHost()
            }
        }
    }
}
