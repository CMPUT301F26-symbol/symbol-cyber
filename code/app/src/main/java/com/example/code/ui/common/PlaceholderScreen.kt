/*
 * PlaceholderScreen.kt
 * Simple "coming soon" screen used for tabs whose feature is not built yet.
 * Known issues: none. Replace each use with the real screen when its story is done.
 */
package com.example.code.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shows [title] and a short note that the screen is not built yet.
 *
 * @param title screen name, e.g. "Reservations".
 * @param note optional extra line, e.g. the user stories this screen will cover.
 */
@Composable
fun PlaceholderScreen(title: String, note: String = "", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text("Coming soon", style = MaterialTheme.typography.bodyMedium)
        if (note.isNotEmpty()) {
            Text(note, style = MaterialTheme.typography.bodySmall)
        }
    }
}
