/*
 * AdminProfileRemovalScreen.kt
 * Selects and confirms removal of an existing profile, with live list/error states.
 * Tapping a profile opens its details.
 * Known issues: production use needs an admin custom claim and reviewed server rules; no rules are deployed here.
 */
package com.example.code.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.code.admin.AdminProfileController
import com.example.code.admin.AdminProfilesState
import com.example.code.admin.AdminRemovalResult
import com.example.code.admin.FirebaseAdminAuthorization
import com.example.code.admin.FirestoreAdminProfileRepository
import kotlinx.coroutines.launch

/** Observes authorized profiles and performs only explicitly confirmed removals. */
@Composable
fun AdminProfileRemovalRoute(
    currentProfileId: String,
    controller: AdminProfileController = remember(currentProfileId) {
        AdminProfileController(FirestoreAdminProfileRepository(), FirebaseAdminAuthorization(), currentProfileId)
    },
) {
    var state by remember(controller) { mutableStateOf(AdminProfilesState()) }
    var retry by remember { mutableStateOf(0) }
    var busyId by remember { mutableStateOf<String?>(null) }
    var removedIds by remember { mutableStateOf(emptySet<String>()) }
    var message by remember { mutableStateOf<String?>(null) }
    // The profile the admin tapped to view, or null to show the list
    var openedId by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(controller, retry) { controller.observe().collect { state = it } }
    val profiles = state.profiles.filterNot { it.id in removedIds }

    val opened = openedId
    if (opened != null) {
        // The phone's Back button also returns to the list
        BackHandler { openedId = null }
        AdminProfileDetailsScreen(
            profile = profiles.firstOrNull { it.id == opened },
            isOwnProfile = opened == currentProfileId,
            onBack = { openedId = null },
        )
        return
    }

    AdminProfileRemovalScreen(
        state = state.copy(profiles = profiles),
        currentProfileId = currentProfileId,
        busyId = busyId,
        message = message,
        onProfileClick = { openedId = it },
        onRefresh = { removedIds = emptySet(); message = null; retry++ },
        onConfirmRemove = { id ->
            if (busyId == null) {
                busyId = id
                scope.launch {
                    message = when (controller.remove(id)) {
                        AdminRemovalResult.REMOVED -> { removedIds = removedIds + id; "Profile removed." }
                        AdminRemovalResult.ALREADY_REMOVED -> { removedIds = removedIds + id; "This profile was already removed." }
                        AdminRemovalResult.DENIED -> { retry++; "Administrator access is required to remove profiles." }
                        AdminRemovalResult.INVALID -> "You cannot remove the current profile or an invalid profile."
                        AdminRemovalResult.FAILED -> "Could not remove the profile. It is still available; try again."
                    }
                    busyId = null
                }
            }
        },
    )
}

/**
 * Displays [state]; confirmation is cancelled if the selected profile disappears from live data.
 * @param onProfileClick called with the ID of the profile the admin tapped.
 */
@Composable
fun AdminProfileRemovalScreen(
    state: AdminProfilesState,
    currentProfileId: String,
    busyId: String?,
    message: String?,
    onRefresh: () -> Unit,
    onConfirmRemove: (String) -> Unit,
    onProfileClick: (String) -> Unit = {},
) {
    var pendingId by rememberSaveable { mutableStateOf<String?>(null) }
    val pending = state.profiles.firstOrNull { it.id == pendingId }
    if (pending != null && state.authorized && pending.id != currentProfileId) {
        AlertDialog(
            onDismissRequest = { pendingId = null },
            title = { Text("Remove profile?") },
            text = { Text("Permanently remove ${pending.name.ifBlank { pending.id }}'s profile? This cannot be undone. Campsite and reservation records are retained.") },
            confirmButton = {
                Button(onClick = { pendingId = null; onConfirmRemove(pending.id) }, enabled = busyId == null) { Text("Confirm removal") }
            },
            dismissButton = { TextButton(onClick = { pendingId = null }) { Text("Cancel") } },
        )
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Profiles", style = MaterialTheme.typography.headlineSmall)
        if (message != null) Text(message)
        when {
            state.loading -> { CircularProgressIndicator(); Text("Loading profiles…") }
            state.error != null -> { Text(state.error); Button(onClick = onRefresh) { Text("Retry") } }
            !state.authorized -> Text("Verified administrator access is required to manage profiles.")
            else -> {
                TextButton(onClick = onRefresh, enabled = busyId == null) { Text("Refresh profiles") }
                if (state.profiles.isEmpty()) Text("No active profiles to review.")
                else LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.profiles, key = { it.id }) { profile ->
                        Card(
                            onClick = { onProfileClick(profile.id) },
                            modifier = Modifier.fillMaxWidth().testTag("profileCard-${profile.id}"), // for testing
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(profile.name.ifBlank { "Unnamed profile" }, style = MaterialTheme.typography.titleMedium)
                                if (profile.email.isNotBlank()) Text(profile.email)
                                Text("Profile ID: ${profile.id}")
                                if (profile.id == currentProfileId) Text("Your profile")
                                else Button(
                                    enabled = busyId == null && currentProfileId.isNotBlank(),
                                    onClick = { pendingId = profile.id },
                                ) { Text(if (busyId == profile.id) "Removing…" else "Remove") }
                            }
                        }
                    }
                }
            }
        }
    }
}
