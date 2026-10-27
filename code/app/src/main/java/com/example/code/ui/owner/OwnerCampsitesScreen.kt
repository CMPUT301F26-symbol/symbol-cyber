/*
 * OwnerCampsitesScreen.kt
 * Campsite owner entry screen (mockup O01): displays the owner's campsites
 * and provides navigation to campsite registration.
 * Known issues: owner filtering will be added after the real User model and
 * Firestore persistence are integrated.
 */

package com.example.code.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite

/**
 * Owner home screen showing registered campsites.
 *
 * @param campsites campsites currently registered by the owner.
 * @param onRegisterCampsite called when the owner taps "+ Register campsite".
 */
@Composable
fun OwnerCampsitesScreen(
    campsites: List<Campsite>,
    onRegisterCampsite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "My campsites",
            style = MaterialTheme.typography.headlineSmall,
        )

        Button(
            onClick = onRegisterCampsite,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("+ Register campsite")
        }

        if (campsites.isEmpty()) {
            Text(
                "You have not registered any campsites yet.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = campsites,
                    key = { campsite -> campsite.id.ifBlank { campsite.name } },
                ) { campsite ->
                    OwnerCampsiteCard(campsite)
                }
            }
        }
    }
}

/**
 * Displays basic information about one campsite owned by the user.
 */
@Composable
private fun OwnerCampsiteCard(campsite: Campsite) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                campsite.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(campsite.location)

            Text("Maximum occupancy: ${campsite.maxOccupancy}")
        }
    }
}