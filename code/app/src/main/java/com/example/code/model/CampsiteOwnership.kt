package com.example.code.model

/** An unknown identity must never match legacy records with no assigned owner. */
fun ownedCampsites(campsites: List<Campsite>, ownerId: String): List<Campsite> =
    if (ownerId.isBlank()) emptyList() else campsites.filter { it.ownerId == ownerId }
