/* Displays public reviews and lets an entrant review an eligible completed stay. */
package com.example.code.ui.entrant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.code.data.CampsiteReviewRepository
import com.example.code.data.CampsiteReviewStore
import com.example.code.model.CampsiteReview
import com.example.code.model.MAX_REVIEW_LENGTH
import com.example.code.model.Reservation
import com.example.code.model.reviewableStay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Placed in campsite details after the lottery content. */
@Composable
fun CampsiteReviews(
    campsiteId: String,
    entrantId: String,
    reservations: List<Reservation>,
    repository: CampsiteReviewStore = remember { CampsiteReviewRepository() },
) {
    var rows by remember(campsiteId) { mutableStateOf<List<CampsiteReview>?>(null) }
    var loadFailed by remember(campsiteId) { mutableStateOf(false) }
    var reload by remember(campsiteId) { mutableIntStateOf(0) }
    var draft by rememberSaveable(campsiteId, entrantId) { mutableStateOf("") }
    var posting by remember(campsiteId, entrantId) { mutableStateOf(false) }
    var postedIds by remember(campsiteId, entrantId) { mutableStateOf(emptySet<String>()) }
    var message by remember(campsiteId, entrantId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(campsiteId, repository, reload) {
        loadFailed = false
        rows = null
        try {
            repository.observe(campsiteId).collect { rows = it }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            rows = null
            loadFailed = true
        }
    }

    val reviews = rows
    val stay = reviews?.let {
        reviewableStay(
            reservations, campsiteId, entrantId,
            it.mapTo(mutableSetOf()) { review -> review.reservationId } + postedIds,
            System.currentTimeMillis(),
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Reviews", style = MaterialTheme.typography.titleLarge)
        when {
            loadFailed -> {
                Text("Could not load reviews.")
                TextButton(onClick = { reload++ }) { Text("Retry") }
            }
            reviews == null -> Text("Loading reviews…")
            reviews.isEmpty() -> Text("No reviews yet.")
            else -> reviews.forEach { review ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(review.authorName.ifBlank { "Entrant" }, style = MaterialTheme.typography.titleSmall)
                        Text(review.body)
                    }
                }
            }
        }
        if (stay != null) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                enabled = !posting,
                label = { Text("Write a review") },
                supportingText = { Text("${draft.trim().length} / $MAX_REVIEW_LENGTH") },
                isError = draft.trim().length > MAX_REVIEW_LENGTH,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                enabled = !posting && draft.isNotBlank() && draft.trim().length <= MAX_REVIEW_LENGTH,
                onClick = {
                    posting = true
                    message = null
                    scope.launch {
                        try {
                            repository.post(campsiteId, stay.id, entrantId, draft.trim())
                            postedIds = postedIds + stay.id
                            draft = ""
                            message = "Review posted."
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            message = "Could not post your review. Check your connection and try again."
                        } finally {
                            posting = false
                        }
                    }
                },
            ) { Text(if (posting) "Posting…" else "Post review") }
        } else if (reviews != null && message == null) {
            Text("You can review this campsite after a completed stay.")
        }
        message?.let { Text(it) }
    }
}
