// Admin screen that lists every campsite image, opens one for review and lets the admin remove it. Known issue is that only campsite images are shown since profile photos can't be added yet.
package com.example.code.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.code.R
import com.example.code.model.Campsite
import com.example.code.model.campsitesWithImages

/**
 * Admin images tab that lists every campsite image and opens one for review.
 *
 * @param campsites every campsite in the app, the ones without an image are skipped.
 * @param isLoading true while campsites are still loading from Firestore.
 * @param errorMessage the loading error, or null if loading worked.
 * @param onRetry called when the admin taps Retry after an error.
 * @param onRemoveImage called with the campsite whose image to remove and a way to show an error if removing fails.
 */
@Composable
fun AdminImagesScreen(
    campsites: List<Campsite>,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onRemoveImage: (Campsite, (String) -> Unit) -> Unit = { _, _ -> },
) {
    val imageCampsites = campsitesWithImages(campsites)

    // The id of the campsite whose image is open for review, or null for the list
    var openedId by rememberSaveable { mutableStateOf<String?>(null) }

    var removeError by remember { mutableStateOf<String?>(null) }

    val opened = openedId
    if (opened != null) {
        // The phone Back button goes back to the list
        BackHandler { openedId = null }

        AdminImageDetails(
            campsite = imageCampsites.firstOrNull { it.id == opened },
            onBack = { openedId = null },
            onRemove = { campsite ->
                removeError = null

                // Go back to the list and show the error there if removing fails
                openedId = null
                onRemoveImage(campsite) { message ->
                    removeError = message
                }
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Uploaded images", style = MaterialTheme.typography.headlineSmall)

        if (removeError != null) {
            Text(removeError!!)
        }

        when {
            // Still waiting for Firestore
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading images…")
            }

            // Firestore failed, so let the admin try again
            errorMessage != null -> {
                Text("Unable to load images. Please try again.")
                TextButton(onClick = onRetry) { Text("Retry") }
            }

            // Loaded, but no campsite has an image
            imageCampsites.isEmpty() -> Text("No uploaded images yet.")

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = imageCampsites, key = { it.id }) { campsite ->
                    AdminImageCard(
                        campsite = campsite,
                        onClick = { openedId = campsite.id },
                    )
                }
            }
        }
    }
}

/**
 * Displays one image with the name of its campsite, and can be tapped to review it.
 *
 * @param campsite the campsite the image belongs to.
 * @param onClick called when the admin taps the card.
 */
@Composable
private fun AdminImageCard(campsite: Campsite, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("imageCard-${campsite.id}"), // for testing
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CampsiteImage(campsite, height = 140)
            Text("Image for ${campsite.name}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Shows one image large with its campsite details, and lets the admin remove it.
 *
 * @param campsite the campsite the image belongs to, or null if the image no longer exists.
 * @param onBack called when the admin taps Back.
 * @param onRemove called with the campsite once the admin confirms removing its image.
 */
@Composable
private fun AdminImageDetails(campsite: Campsite?, onBack: () -> Unit, onRemove: (Campsite) -> Unit) {
    // True while the confirm dialog is showing
    var confirming by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.testTag("backButton"), // for testing
        ) {
            Text("Back")
        }

        // The image or its campsite was removed while it was open
        if (campsite == null) {
            Text("Image not found. It may have been removed.")
            return@Column
        }

        Text("Image for ${campsite.name}", style = MaterialTheme.typography.headlineSmall)
        CampsiteImage(campsite, height = 260)

        Text("Campsite", style = MaterialTheme.typography.titleMedium)
        Text(campsite.name)
        Text("Location", style = MaterialTheme.typography.titleMedium)
        Text(campsite.location.ifBlank { "Not provided" })
        Text("Image link", style = MaterialTheme.typography.titleMedium)
        Text(campsite.imageUrl.orEmpty())

        Button(
            onClick = { confirming = true },
            modifier = Modifier.testTag("removeImageButton"), // for testing
        ) {
            Text("Remove image")
        }
    }

    // Ask before removing, since it can't be undone
    if (confirming && campsite != null) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Remove image for ${campsite.name}?") },
            text = { Text("The campsite stays, but its image is removed. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirming = false
                        onRemove(campsite)
                    },
                    modifier = Modifier.testTag("confirmRemoveImageButton"), // for testing
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirming = false },
                    modifier = Modifier.testTag("cancelRemoveImageButton"), // for testing
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Loads a campsite image from its link, showing a placeholder if it can't be loaded.
 *
 * @param campsite the campsite whose image to show.
 * @param height how tall the image is, in dp.
 */
@Composable
private fun CampsiteImage(campsite: Campsite, height: Int) {
    val placeholder = painterResource(R.drawable.campsite_placeholder)
    AsyncImage(
        model = campsite.imageUrl,
        contentDescription = "Uploaded image for ${campsite.name}",
        placeholder = placeholder,
        fallback = placeholder,
        error = placeholder,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(height.dp),
    )
}
