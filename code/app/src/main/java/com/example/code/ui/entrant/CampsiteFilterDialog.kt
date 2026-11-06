/*
 * CampsiteFilterDialog.kt
 * Collects optional location, capacity and inclusive stay dates for the browse screen.
 * Known issues: dates use YYYY-MM-DD in the device's calendar time zone, as owner dates do.
 */
package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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

/** Calls [onApply] with the draft; a validation exception keeps the dialog open. */
@Composable
fun CampsiteFilterDialog(
    initial: List<String>,
    onApply: (String, String, String, String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var location by rememberSaveable { mutableStateOf(initial[0]) }
    var guests by rememberSaveable { mutableStateOf(initial[1]) }
    var start by rememberSaveable { mutableStateOf(initial[2]) }
    var end by rememberSaveable { mutableStateOf(initial[3]) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter campsites") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Leave any field empty for no restriction. Dates match advertised stays and exclude booked dates.")
                OutlinedTextField(location, { location = it }, label = { Text("Location") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(guests, { guests = it }, label = { Text("Guests") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(start, { start = it }, label = { Text("Start date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(end, { end = it }, label = { Text("End date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it) }
                TextButton(onClick = onClear) { Text("Clear filters") }
            }
        },
        confirmButton = {
            Button(onClick = {
                try { onApply(location, guests, start, end) }
                catch (invalid: IllegalArgumentException) { error = invalid.message }
            }) { Text("Apply filters") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
