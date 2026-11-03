package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite
import com.example.code.model.searchCampsites

/** Browses and searches the latest campsites without discarding the query on return. */
@Composable
fun ExploreScreen(
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = searchCampsites(campsites, query)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search campsites") },
            placeholder = { Text("Name, location or area") },
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    TextButton(onClick = { query = "" }) { Text("Clear search") }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        when {
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading campsites…")
            }
            errorMessage != null -> {
                Text("Unable to load campsites. Please try again.")
                TextButton(onClick = onRetry) { Text("Retry") }
            }
            results.isEmpty() -> Text(
                if (query.isBlank()) "No campsites available yet."
                else "No results found. Try another name or location."
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = results, key = { it.id }) { campsite ->
                    CampsiteCard(campsite = campsite, onClick = { onCampsiteClick(campsite) })
                }
            }
        }
    }
}
