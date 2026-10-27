/*
 * OwnerCampsitesScreen.kt
 * Campsite owner entry screen (mockup O01): list of the owner's campsites
 * and a button to register a new one.
 * Known issues: list is empty and the button does nothing until T5
 * (US 02.01.01, 02.01.02) and US 02.03.01 are done.
 */
package com.example.code.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Owner home: "My campsites".
 *
 * @param onRegisterCampsite called when the owner taps "+ Register campsite".
 */
@Composable
fun OwnerCampsitesScreen(
    onRegisterCampsite: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("My campsites", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onRegisterCampsite, modifier = Modifier.fillMaxWidth()) {
            Text("+ Register campsite")
        }
        Text(
            "You have not registered any campsites yet.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
