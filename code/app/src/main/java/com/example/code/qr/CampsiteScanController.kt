/*
 * CampsiteScanController.kt
 * Rejects foreign QR payloads and checks existence before allowing navigation to a campsite.
 * Known issues: a physical-device camera smoke check is still required for the scanner integration.
 */
package com.example.code.qr

import kotlinx.coroutines.CancellationException

/** Outcome of validating and resolving a decoded promotional QR payload. */
sealed interface CampsiteScanResult {
    /** A server-verified target; [campsiteId] is the exact saved document ID. */
    data class Found(val campsiteId: String) : CampsiteScanResult
    /** Payload does not use the supported Symbol campsite link format. */
    data object Invalid : CampsiteScanResult
    /** Valid link, but its campsite was removed or does not exist. */
    data object Missing : CampsiteScanResult
    /** Target could not be verified, for example due to network or read permissions. */
    data object Unavailable : CampsiteScanResult
}

/** Resolves scanned links through [lookup]; no arbitrary URL is opened. */
class CampsiteScanController(private val lookup: CampsiteLookup) {
    /** Validates [payload], verifies existence and preserves coroutine cancellation. */
    suspend fun resolve(payload: String): CampsiteScanResult {
        val id = CampsiteQrLink.decode(payload) ?: return CampsiteScanResult.Invalid
        return try {
            if (lookup.exists(id)) CampsiteScanResult.Found(id) else CampsiteScanResult.Missing
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            CampsiteScanResult.Unavailable
        }
    }
}
