package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CampsiteOwnershipTest {
    private val sites = listOf(
        Campsite(id = "mine", ownerId = "owner-a"),
        Campsite(id = "other", ownerId = "owner-b"),
        Campsite(id = "legacy", ownerId = ""),
    )

    @Test fun ownerCanOnlyResolveTheirOwnRecords() {
        assertEquals(listOf(sites[0]), ownedCampsites(sites, "owner-a"))
        assertEquals(emptyList<Campsite>(), ownedCampsites(sites, "unknown"))
    }

    @Test fun missingIdentityNeverExposesLegacyRecords() {
        assertEquals(emptyList<Campsite>(), ownedCampsites(sites, ""))
        assertEquals(emptyList<Campsite>(), ownedCampsites(sites, " "))
    }

    @Test fun changedOwnershipAndRemovalInvalidatePreviouslyOwnedRecords() {
        assertEquals(emptyList<Campsite>(), ownedCampsites(listOf(sites[0].copy(ownerId = "owner-b")), "owner-a"))
        assertEquals(emptyList<Campsite>(), ownedCampsites(emptyList(), "owner-a"))
    }
}
