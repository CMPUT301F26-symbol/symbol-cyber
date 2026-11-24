/* Exercises review submission and admin log tabs with isolated test data. */
package com.example.code.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.code.admin.AdminLogController
import com.example.code.admin.AdminLogEntry
import com.example.code.data.CampsiteReviewStore
import com.example.code.model.CampsiteReview
import com.example.code.model.Reservation
import com.example.code.ui.admin.AdminLogsScreen
import com.example.code.ui.entrant.CampsiteReviews
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DeanStoryScreensTest {
    @get:Rule val compose = createComposeRule()

    private class Reviews : CampsiteReviewStore {
        private val reviews = MutableStateFlow<List<CampsiteReview>>(emptyList())
        var submitted: Pair<String, String>? = null

        override fun observe(campsiteId: String) = reviews

        override suspend fun post(campsiteId: String, reservationId: String, entrantId: String, text: String) {
            submitted = reservationId to entrantId
            reviews.value = listOf(CampsiteReview(reservationId, "Avery", text, 1L))
        }
    }

    @Test
    fun completedStayCanBeReviewedOnce() {
        val store = Reviews()
        val stay = Reservation(id = "stay-1", campsiteId = "pine-lake", entrantId = "entrant-1", endDate = 1L)
        compose.setContent {
            MaterialTheme { CampsiteReviews("pine-lake", "entrant-1", listOf(stay), store) }
        }
        compose.onNodeWithText("Write a review").performTextInput("Quiet tent site.")
        compose.onNodeWithText("Post review").performClick()
        compose.onNodeWithText("Quiet tent site.").assertIsDisplayed()
        compose.onNodeWithText("Review posted.").assertIsDisplayed()
        compose.onNodeWithText("Post review").assertDoesNotExist()
        compose.runOnIdle { assertEquals("stay-1" to "entrant-1", store.submitted) }
    }

    @Test
    fun noCompletedStayMeansNoPostingControl() {
        val store = Reviews()
        val otherStay = Reservation(id = "stay-2", campsiteId = "pine-lake", entrantId = "other", endDate = 1L)
        compose.setContent {
            MaterialTheme { CampsiteReviews("pine-lake", "entrant-1", listOf(otherStay), store) }
        }
        compose.onNodeWithText("No reviews yet.").assertIsDisplayed()
        compose.onNodeWithText("Post review").assertDoesNotExist()
        compose.runOnIdle { assertEquals(null, store.submitted) }
    }

    @Test
    fun adminSeesNotificationsAndReservationParticipants() {
        val controller = AdminLogController { true }
        val notice = AdminLogEntry("note", "Lottery winner", "You won!", "System", "entrant-1", "Pine Lake", null)
        val message = AdminLogEntry("msg", "Pine Lake", "Arrive after 3.", "owner-1", "entrant-1", "Reservation: stay-1", null)
        compose.setContent {
            MaterialTheme { AdminLogsScreen(controller, { listOf(notice) }, { listOf(message) }) }
        }
        compose.onNodeWithText("Lottery winner").assertIsDisplayed()
        compose.onNodeWithText("Reservation messages").performClick()
        compose.onNodeWithText("Arrive after 3.").assertIsDisplayed()
        compose.onNodeWithText("Owner: owner-1").assertIsDisplayed()
        compose.onNodeWithText("Entrant: entrant-1").assertIsDisplayed()
        compose.onNodeWithText("Reservation: stay-1").assertIsDisplayed()
        compose.onNodeWithText("Lottery winner").assertDoesNotExist()
    }

    @Test
    fun noAdministratorClaimPreventsBothLogReads() {
        val controller = AdminLogController { false }
        var reads = 0
        compose.setContent {
            MaterialTheme {
                AdminLogsScreen(controller, { reads++; emptyList() }, { reads++; emptyList() })
            }
        }
        compose.onNodeWithText("Verified administrator access is required to view logs.").assertIsDisplayed()
        compose.onNodeWithText("Reservation messages").performClick()
        compose.onNodeWithText("Verified administrator access is required to view logs.").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, reads) }
    }
}
