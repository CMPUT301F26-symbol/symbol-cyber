package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.code.R
import com.example.code.model.Campsite
import java.util.Locale

/** Resolves the selected ID from live records, so edits and removal update an open screen. */
@Composable
fun CampsiteDetailsDestination(
    campsiteId: String?,
    campsites: List<Campsite>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    val campsite = campsites.firstOrNull { it.id == campsiteId }
    when {
        errorMessage != null -> DetailsStatus(onBack) {
            Text("Unable to load campsite details. Please try again.")
            Button(onClick = onRetry) { Text("Retry") }
        }
        isLoading -> DetailsStatus(onBack) {
            CircularProgressIndicator()
            Text("Loading campsite details…")
        }
        campsite == null -> DetailsStatus(onBack) {
            Text("Campsite not found. It may have been removed.")
        }
        else -> CampsiteDetailsScreen(campsite, onBack)
    }
}

@Composable
private fun DetailsStatus(onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) { Text("Back") }
        content()
    }
}

/** Full stored information, shared by entrant and owner detail views. */
@Composable
fun CampsiteDetailsScreen(campsite: Campsite, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text(campsite.name, style = MaterialTheme.typography.headlineSmall)
        val placeholder = painterResource(R.drawable.campsite_placeholder)
        AsyncImage(
            model = campsite.imageUrl?.takeIf { it.isNotBlank() },
            contentDescription = if (campsite.imageUrl.isNullOrBlank())
                "No promotional image available" else "Promotional image for ${campsite.name}",
            placeholder = placeholder,
            fallback = placeholder,
            error = placeholder,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(180.dp),
        )
        DetailField("Location", campsite.location)
        DetailField("Description / amenities", campsite.description)
        DetailField("Maximum occupancy", campsite.maxOccupancy.toString())
        DetailField("Price", campsitePriceText(campsite.price, campsite.currency))
        DetailField("Rating", campsiteRatingText(campsite.rating))
        DetailField("Map location", "${campsite.latitude}, ${campsite.longitude}")
        DetailField("Tags", campsite.tags.joinToString(", ").ifBlank { "No tags" })
        DetailField("Rules", campsite.rules.ifBlank { "No rules provided" })
        DetailField("Cancellation policy", campsite.cancellationPolicy.ifBlank { "No cancellation policy provided" })
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Text(label, style = MaterialTheme.typography.titleMedium)
    Text(value.ifBlank { "Not provided" })
}

/** Do not invent a currency or a price for older campsite records. */
internal fun campsitePriceText(price: Double?, currency: String?): String {
    if (price == null || !price.isFinite() || price < 0) return "Price not provided"
    val amount = String.format(Locale.getDefault(), "%.2f", price)
    return listOfNotNull(currency?.trim()?.takeIf { it.isNotEmpty() }, amount).joinToString(" ")
}
