// Form where an owner registers a new campsite. Known issues: image is a link, not an upload.
// AI use: Claude (Anthropic) helped find and fix the double-tap duplicate save bug (isSaving).

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite
import com.example.code.model.campsiteFromForm

/**
 * Displays the campsite registration form.
 *
 * @param onRegister called with the new campsite and a way to show an error if saving fails
 * @param onBack called when the owner goes back without registering
 */
@Composable
fun RegisterCampsiteScreen(
    onRegister: (Campsite, (String) -> Unit) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxOccupancy by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // True while the campsite is being saved, so it can't be saved twice
    var isSaving by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxWidth().testTag("nameInput"), // for testing
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth().testTag("locationInput"), // for testing
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / amenities") },
            modifier = Modifier.fillMaxWidth().testTag("descriptionInput"), // for testing
        )

        OutlinedTextField(
            value = maxOccupancy,
            onValueChange = { maxOccupancy = it },
            label = { Text("Maximum occupancy") },
            modifier = Modifier.fillMaxWidth().testTag("maxOccupancyInput"), // for testing
        )

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth().testTag("latitudeInput"), // for testing
        )

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth().testTag("longitudeInput"), // for testing
        )

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma separated, optional)") },
            modifier = Modifier.fillMaxWidth().testTag("tagsInput"), // for testing
        )

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL (optional)") },
            modifier = Modifier.fillMaxWidth().testTag("imageUrlInput"), // for testing
        )

        if (errorMessage != null) {
            Text(errorMessage!!)
        }

        Button(
            onClick = {
                val campsite = campsiteFromForm(
                    name, location, description, maxOccupancy,
                    latitude, longitude, tags, imageUrl,
                )

                if (campsite == null) {
                    errorMessage = "Please complete all required fields with valid values."
                } else {
                    errorMessage = null
                    isSaving = true

                    // Show the error if saving fails and let them try again
                    onRegister(campsite) { message ->
                        errorMessage = message
                        isSaving = false
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().testTag("registerButton"), // for testing
        ) {
            Text("Register campsite")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().testTag("backButton"), // for testing
        ) {
            Text("Back")
        }
    }
}