/*
 * User.kt
 * Represents a device-based user profile and the roles that profile may use.
 * Stores profile information and notification preferences shared across app modes.
 * Role-based navigation uses this profile's assigned roles.
 */

package com.example.code

/**
 * Roles that may be associated with a [User].
 */
enum class UserRole {
    ENTRANT,
    CAMPSITE_OWNER,
    ADMIN,
}

/**
 * Represents one application user identified by a device ID.
 *
 * A single user profile may contain multiple roles instead of maintaining
 * separate entrant, campsite-owner, and administrator profiles.
 *
 * @property deviceId unique device identifier used to identify the user.
 * @property name user's display name.
 * @property email user's email address.
 * @property phoneNumber user's phone number.
 * @property profilePhotoUrl optional reference to the user's profile image.
 * @property notificationsEnabled whether the user allows notifications.
 * @property roles roles associated with this user profile.
 */
class User(
    val deviceId: String,
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var profilePhotoUrl: String? = null,
    var notificationsEnabled: Boolean = true,
    val roles: Set<UserRole>,
) {

    /**
     * Updates the user's editable profile information.
     *
     * @param name new display name.
     * @param email new email address.
     * @param phoneNumber new phone number.
     * @param profilePhotoUrl new profile image reference, or the existing
     * reference when not supplied.
     */
    fun updateProfile(
        name: String,
        email: String,
        phoneNumber: String,
        profilePhotoUrl: String? = this.profilePhotoUrl,
    ) {
        this.name = name
        this.email = email
        this.phoneNumber = phoneNumber
        this.profilePhotoUrl = profilePhotoUrl
    }

    /**
     * Changes whether this user has notifications enabled.
     *
     * @param enabled true to enable notifications, false to opt out.
     */
    fun updateNotificationPreference(enabled: Boolean) {
        notificationsEnabled = enabled
    }

    /**
     * Returns whether this user has [role].
     */
    fun canUseRole(role: UserRole): Boolean {
        return role in roles
    }
}