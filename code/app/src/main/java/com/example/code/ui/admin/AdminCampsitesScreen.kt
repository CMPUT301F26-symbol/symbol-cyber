/*
 * AdminCampsitesScreen.kt
 * Admin entry screen (mockup A01): all campsites in the app, each with a
 * Remove action.
 * Known issues: list is empty until campsites are read from Firestore
 * (US 03.04.01) and removal is built (US 03.01.01).
 */
package com.example.code.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Admin home: browse all campsites.
 */
@Composable
fun AdminCampsitesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("All campsites", style = MaterialTheme.typography.headlineSmall)
        Text(
            "No campsites to review yet.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
