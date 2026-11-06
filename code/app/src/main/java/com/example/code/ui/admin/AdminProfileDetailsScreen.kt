// Admin screen that shows the information of one selected profile.
package com.example.code.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.code.admin.AdminProfile

/**
 * Shows the name, email, phone and profile ID of a profile.
 *
 * @param profile the profile to show, or null if it no longer exists.
 * @param isOwnProfile true if this is the admin's own profile.
 * @param onBack called when the admin taps Back.
 */
@Composable
fun AdminProfileDetailsScreen(
    profile: AdminProfile?,
    isOwnProfile: Boolean,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.testTag("backButton"), // for testing
        ) {
            Text("Back")
        }

        // The profile was removed while it was open
        if (profile == null) {
            Text("Profile not found. It may have been removed.")
            return@Column
        }

        Text(
            profile.name.ifBlank { "Unnamed profile" },
            style = MaterialTheme.typography.headlineSmall,
        )

        if (isOwnProfile) {
            Text("Your profile")
        }

        ProfileField("Email", profile.email)
        ProfileField("Phone", profile.phone)
        ProfileField("Profile ID", profile.id)
    }
}

/**
 * Displays one labelled piece of profile information.
 *
 * @param label the name of the field.
 * @param value the field's value, shown as "Not provided" if blank.
 */
@Composable
private fun ProfileField(label: String, value: String) {
    Text(label, style = MaterialTheme.typography.titleMedium)
    Text(value.ifBlank { "Not provided" })
}
