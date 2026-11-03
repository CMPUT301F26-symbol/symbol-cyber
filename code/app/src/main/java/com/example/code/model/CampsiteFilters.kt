package com.example.code.model

/**
 * Applied criteria supplied by the filter controls (US 01.01.02).
 *
 * [availableCampsiteIds] is the result of the lottery/date availability lookup:
 * null means no date filter; an empty set means no campsite is available.
 * Dates must not be inferred from campsite coordinates or capacity.
 */
data class CampsiteFilters(
    val location: String = "",
    val guestCount: Int? = null,
    val availableCampsiteIds: Set<String>? = null,
) {
    init {
        require(guestCount == null || guestCount > 0) { "Guest count must be positive." }
    }

    val isActive: Boolean
        get() = location.isNotBlank() || guestCount != null || availableCampsiteIds != null
}

/** Combines all applied filters; evaluate again whenever criteria or records change. */
fun filterCampsites(campsites: List<Campsite>, filters: CampsiteFilters): List<Campsite> =
    campsites.filter {
        it.location.contains(filters.location.trim(), ignoreCase = true) &&
            (filters.guestCount == null || it.maxOccupancy >= filters.guestCount) &&
            (filters.availableCampsiteIds == null || it.id in filters.availableCampsiteIds)
    }
