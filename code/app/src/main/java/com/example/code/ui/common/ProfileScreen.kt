/*
 * ProfileScreen.kt
 * Profile screen shared by all app modes.
 * Allows the user to edit personal information and switch between available modes.
 * Known issues: available modes are not yet tied to the real user's roles.
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.code.User
import com.example.code.navigation.AppMode

/**
 * Profile screen for viewing and editing the current user's information.
 *
 * @param user currently loaded user profile.
 * @param activeMode mode the app is in now.
 * @param availableModes modes the user may switch to.
 * @param isSaving whether a profile save is in progress.
 * @param errorMessage validation or persistence error to display.
 * @param onSaveProfile called when the user saves profile information.
 * @param onSwitchMode called when the user taps a different mode.
 */
@Composable
fun ProfileScreen(
    user: User,
    activeMode: AppMode,
    availableModes: List<AppMode>,
    isSaving: Boolean,
    errorMessage: String?,
    onSaveProfile: (String, String, String) -> Unit,
    onSwitchMode: (AppMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember(user.deviceId, user.name) {
        mutableStateOf(user.name)
    }

    var email by remember(user.deviceId, user.email) {
        mutableStateOf(user.email)
    }

    var phoneNumber by remember(user.deviceId, user.phoneNumber) {
        mutableStateOf(user.phoneNumber)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = {
                onSaveProfile(
                    name,
                    email,
                    phoneNumber,
                )
            },
            enabled = !isSaving,
        ) {
            if (isSaving) {
                CircularProgressIndicator()
            } else {
                Text("Save")
            }
        }

        Text(
            text = "Mode",
            style = MaterialTheme.typography.titleMedium,
        )

        Column(
            modifier = Modifier.selectableGroup(),
        ) {
            availableModes.forEach { mode ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == activeMode,
                                onClick = { onSwitchMode(mode) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = mode == activeMode,
                        onClick = null,
                    )

                    Text(
                        text = mode.label,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        Text(
            text = "Admin mode only shows for admin profiles.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}