/*
 * CampsiteLookup.kt
 * Checks the saved target of a scanned code using a server read, independently of browse loading.
 * Known issues: requires network access; failure is reported instead of accepting an unverified target.
 */
package com.example.code.qr

import com.example.code.data.awaitResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/** Lookup boundary for QR resolution; tests supply a fake without contacting shared Firebase. */
fun interface CampsiteLookup {
    /** Returns whether [campsiteId] currently exists; throws when it cannot be checked. */
    suspend fun exists(campsiteId: String): Boolean
}

/** Reads the existing campsite collection without changing a document. */
class FirestoreCampsiteLookup(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : CampsiteLookup {
    override suspend fun exists(campsiteId: String): Boolean =
        db.collection("campsites").document(campsiteId).get(Source.SERVER).awaitResult().exists()
}
