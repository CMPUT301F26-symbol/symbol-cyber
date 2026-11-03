package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CampsiteFiltersTest {
    private val sites = listOf(
        Campsite(id = "1", name = "Pine", location = "Banff", maxOccupancy = 4),
        Campsite(id = "2", name = "Lake", location = "Banff", maxOccupancy = 2),
        Campsite(id = "3", name = "Pine", location = "Jasper", maxOccupancy = 6),
    )

    @Test fun combinesSearchLocationGuestsAndDateMatches() {
        val filters = CampsiteFilters(" bAnFf ", 4, setOf("1", "3"))
        assertEquals(listOf(sites[0]), filterCampsites(searchCampsites(sites, "PINE"), filters))
    }

    @Test fun noDateFilterDiffersFromNoAvailableDates() {
        assertEquals(sites, filterCampsites(sites, CampsiteFilters()))
        assertEquals(emptyList<Campsite>(), filterCampsites(sites, CampsiteFilters(availableCampsiteIds = emptySet())))
    }

    @Test fun capacityBoundaryIsInclusiveAndChangedDataReevaluated() {
        val filters = CampsiteFilters(guestCount = 4)
        assertEquals(listOf(sites[0], sites[2]), filterCampsites(sites, filters))
        assertEquals(listOf(sites[2]), filterCampsites(listOf(sites[0].copy(maxOccupancy = 3), sites[2]), filters))
        assertEquals(listOf(sites[2]), filterCampsites(sites, filters.copy(location = "Jasper")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidGuestCount() { CampsiteFilters(guestCount = 0) }
}
