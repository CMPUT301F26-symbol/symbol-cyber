package com.example.code.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CampsiteSearchTest {
    private val sites = listOf(
        Campsite(id = "1", name = "Pine Ridge", location = "Banff"),
        Campsite(id = "2", name = "Jasper Lake", location = "Alberta"),
    )

    @Test fun matchesNameOrLocationIgnoringCaseAndOuterWhitespace() {
        assertEquals(listOf(sites[0]), searchCampsites(sites, " bAnFf "))
        assertEquals(listOf(sites[1]), searchCampsites(sites, "LAKE"))
    }

    @Test fun blankSearchRestoresDefaultListAndUnknownKeywordReturnsNoMatches() {
        assertEquals(sites, searchCampsites(sites, "  "))
        assertEquals(emptyList<Campsite>(), searchCampsites(sites, "unknown"))
    }

    @Test fun newSnapshotsAreSearchedWithTheSameQuery() {
        assertEquals(emptyList<Campsite>(), searchCampsites(sites, "Creek"))
        val updated = sites + Campsite(id = "3", name = "Creek")
        assertEquals(listOf(updated.last()), searchCampsites(updated, "Creek"))
    }
}
