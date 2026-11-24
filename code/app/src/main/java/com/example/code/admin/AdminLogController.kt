/* Loads persisted audit records after verifying the Firebase administrator claim. */
package com.example.code.admin

import kotlinx.coroutines.CancellationException

/** A read-only audit row. IDs are retained even when participant profiles are gone. */
data class AdminLogEntry(
    val id: String,
    val title: String,
    val body: String,
    val sender: String,
    val recipient: String,
    val context: String,
    val sentAt: Long?,
)

sealed interface AdminLogResult {
    data object Denied : AdminLogResult
    data object Failed : AdminLogResult
    data class Loaded(val rows: List<AdminLogEntry>) : AdminLogResult
}

/** Verifies access before invoking any log source. */
class AdminLogController(private val authorization: AdminAuthorization) {
    suspend fun load(read: suspend () -> List<AdminLogEntry>): AdminLogResult = try {
        if (!authorization.isAdmin()) AdminLogResult.Denied
        else AdminLogResult.Loaded(
            read().sortedWith(compareByDescending<AdminLogEntry> { it.sentAt }.thenBy { it.id }),
        )
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        AdminLogResult.Failed
    }
}
