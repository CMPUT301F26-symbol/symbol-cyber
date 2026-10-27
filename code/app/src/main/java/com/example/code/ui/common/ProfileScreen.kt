/*
 * ProfileScreen.kt
 * Profile screen shared by all modes (mockup E12). For now it only has the
 * mode switch (US 03.11.01). Profile fields, notifications opt-out, app
 * policy and delete profile come with the User model (T3) and later stories.
 * Known issues: mode list is not tied to the real user's roles yet.
 */
package com.example.code.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.code.navigation.AppMode

/**
 * Profile screen with the mode switch.
 *
 * @param activeMode mode the app is in now.
 * @param availableModes modes the user may switch to.
 * @param onSwitchMode called when the user taps a different mode.
 */
@Composable
fun ProfileScreen(
    activeMode: AppMode,
    availableModes: List<AppMode>,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Text("Mode", style = MaterialTheme.typography.titleMedium)
        Column(modifier = Modifier.selectableGroup()) {
            availableModes.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = mode == activeMode,
                            onClick = { onSwitchMode(mode) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = mode == activeMode, onClick = null)
                    Text(mode.label, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        Text(
            "Admin mode only shows for admin profiles.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
