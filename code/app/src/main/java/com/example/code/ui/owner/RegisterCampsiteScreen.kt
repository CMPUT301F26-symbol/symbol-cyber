/*
 * RegisterCampsiteScreen.kt
 * Allows a campsite owner to enter the information required to register a campsite.
 * Known issues: registration is not yet persisted to Firestore and image selection
 * is not yet implemented.
 */

package com.example.code.ui.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite

/**
 * Displays the campsite registration form.
 *
 * @param onRegister called with the new campsite after valid information is submitted.
 * @param onBack called when the owner chooses to return without registering.
 */
@Composable
fun RegisterCampsiteScreen(
    onRegister: (Campsite) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxOccupancy by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Register campsite")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / amenities") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = maxOccupancy,
            onValueChange = { maxOccupancy = it },
            label = { Text("Maximum occupancy") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma separated, optional)") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (errorMessage != null) {
            Text(errorMessage!!)
        }

        Button(
            onClick = {
                val occupancyValue = maxOccupancy.toIntOrNull()
                val latitudeValue = latitude.toDoubleOrNull()
                val longitudeValue = longitude.toDoubleOrNull()

                if (
                    name.isBlank() ||
                    location.isBlank() ||
                    description.isBlank() ||
                    occupancyValue == null ||
                    occupancyValue <= 0 ||
                    latitudeValue == null ||
                    longitudeValue == null
                ) {
                    errorMessage = "Please complete all required fields with valid values."
                } else {
                    errorMessage = null

                    val campsite = Campsite(
                        name = name.trim(),
                        location = location.trim(),
                        description = description.trim(),
                        maxOccupancy = occupancyValue,
                        latitude = latitudeValue,
                        longitude = longitudeValue,
                        tags = tags
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() },
                    )

                    onRegister(campsite)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Register campsite")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Back")
        }
    }
}