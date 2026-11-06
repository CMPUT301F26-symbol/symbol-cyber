/*
 * CampsiteQrLink.kt
 * Defines the versioned, ID-only promotional link shared by QR generation and scanning.
 * Known issues: only this app's v1 links are accepted; arbitrary web links are never followed.
 */
package com.example.code.qr

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

/** Encodes a Firestore document ID without including personal or mutable campsite details. */
object CampsiteQrLink {
    private const val PREFIX = "symbol://campsite/v1/"

    /**
     * Returns the canonical link for [campsiteId].
     * @throws IllegalArgumentException for a blank or invalid Firestore document ID.
     */
    fun encode(campsiteId: String): String {
        require(campsiteId.isNotBlank() && campsiteId != "." && campsiteId != "..") { "A saved campsite ID is required." }
        require('/' !in campsiteId && campsiteId.none { it.isISOControl() }) { "Invalid campsite ID." }
        require(campsiteId.toByteArray(Charsets.UTF_8).size <= 1500) { "Campsite ID is too long." }
        return PREFIX + URLEncoder.encode(campsiteId, "UTF-8").replace("+", "%20")
    }

    /** Returns a canonical v1 campsite ID, or null for malformed/foreign payloads. */
    fun decode(payload: String): String? = runCatching {
        val text = payload.trim()
        require(text.length <= 8192)
        val uri = URI(text)
        require(uri.scheme == "symbol" && uri.rawAuthority == "campsite")
        require(uri.rawQuery == null && uri.rawFragment == null)
        require(uri.rawPath.startsWith("/v1/"))
        val segment = uri.rawPath.removePrefix("/v1/")
        require('/' !in segment)
        val id = URLDecoder.decode(segment, "UTF-8")
        require(encode(id) == text)
        id
    }.getOrNull()
}
