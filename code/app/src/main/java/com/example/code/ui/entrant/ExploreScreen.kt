/*
 * ExploreScreen.kt
 * Searches live campsite records and applies validated criteria from the filter dialog.
 * Known issues: dates depend on published lottery periods and reservations supplied by navigation.
 */
package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import com.example.code.controller.CampsiteFilterController
import com.example.code.model.CampsiteFilters
import com.example.code.model.filterCampsites
import com.example.code.model.searchCampsites

/**
 * Browses the latest records using search and the applied filter state.
 * Filter controls supply [filters]; date availability is resolved by their lottery lookup.
 */
@Composable
fun ExploreScreen(
    campsites: List<Campsite>,
    onCampsiteClick: (Campsite) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    filters: CampsiteFilters = CampsiteFilters(),
    onClearFilters: (() -> Unit)? = null,
    lotteries: List<Lottery> = emptyList(),
    reservations: List<Reservation> = emptyList(),
    availabilityReady: Boolean = true,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var guests by rememberSaveable { mutableStateOf("") }
    var start by rememberSaveable { mutableStateOf("") }
    var end by rememberSaveable { mutableStateOf("") }
    var editingFilters by rememberSaveable { mutableStateOf(false) }
    val filterController = remember { CampsiteFilterController() }
    val selection = filterController.parse(location, guests, start, end)
    val filtersActive = filters.isActive || selection.isActive
    val clearFilters = {
        location = ""; guests = ""; start = ""; end = ""; query = ""
        onClearFilters?.invoke()
        editingFilters = false
    }
    val results = filterController.apply(
        filterCampsites(searchCampsites(campsites, query), filters), selection, lotteries, reservations,
    )
    if (editingFilters) {
        CampsiteFilterDialog(
            initial = listOf(location, guests, start, end),
            onApply = { nextLocation, nextGuests, nextStart, nextEnd ->
                filterController.parse(nextLocation, nextGuests, nextStart, nextEnd)
                location = nextLocation; guests = nextGuests; start = nextStart; end = nextEnd
                editingFilters = false
            },
            onClear = clearFilters,
            onDismiss = { editingFilters = false },
        )
    }
    val listState = rememberLazyListState()
    LaunchedEffect(query, filters, location, guests, start, end) { listState.scrollToItem(0) }

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
        TextButton(onClick = { editingFilters = true }) { Text("Filters") }
        if (selection.isActive) {
            Text(listOfNotNull(
                location.trim().takeIf { it.isNotEmpty() }?.let { "Location: $it" },
                selection.guests?.let { "Guests: $it" },
                selection.stay?.let { "$start to $end" },
            ).joinToString(" • "))
            TextButton(onClick = clearFilters) { Text("Clear filters") }
        }
        if (filters.isActive) {
            Text(buildList {
                if (filters.location.isNotBlank()) add("Location: ${filters.location.trim()}")
                filters.guestCount?.let { add("Guests: $it") }
                if (filters.availableCampsiteIds != null) add("Dates applied")
            }.joinToString(" • "))
            if (onClearFilters != null) {
                TextButton(onClick = onClearFilters) { Text("Clear filters") }
            }
        }
        when {
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading campsites…")
            }
            errorMessage != null -> {
                Text("Unable to load campsites. Please try again.")
                TextButton(onClick = onRetry) { Text("Retry") }
            }
            selection.stay != null && !availabilityReady -> {
                CircularProgressIndicator()
                Text("Loading availability…")
            }
            results.isEmpty() -> Text(
                if (filtersActive) "No campsites match your current filters."
                else if (query.isBlank()) "No campsites available yet."
                else "No results found. Try another name or location."
            )
            else -> {
                Text("${results.size} campsite${if (results.size == 1) "" else "s"} found")
                LazyColumn(
                    state = listState,
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
}
