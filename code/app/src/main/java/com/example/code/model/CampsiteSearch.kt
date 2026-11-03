package com.example.code.model

/** Keyword search over the latest Firestore records. Blank input restores the full list. */
fun searchCampsites(campsites: List<Campsite>, query: String): List<Campsite> {
    val keyword = query.trim()
    if (keyword.isEmpty()) return campsites
    return campsites.filter {
        it.name.contains(keyword, ignoreCase = true) ||
            it.location.contains(keyword, ignoreCase = true)
    }
}
