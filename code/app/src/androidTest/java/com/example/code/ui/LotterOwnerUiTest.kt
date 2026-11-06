package com.example.code.ui

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.code.controller.LotteryController
import com.example.code.model.Lottery
import com.example.code.ui.common.formatDateTime
import com.example.code.ui.owner.OwnerCampsiteDetailsDestination
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.and
import kotlin.collections.get

/** AI USAGE
 * The basis for the following UI intent test bench file was generated using
 * GPT6-Sol "Please create a basic set of UI intent tests for my lottery files", 11-05-2026
 *
 */


/**
 * Instrumented owner user-flow tests for the assignment's "intent tests" requirement.
 * They interact with the actual owner destination, form, and native pickers.
 * They exercise the real LotteryController with fake persistence, not live Firestore.
 *
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LotteryOwnerUiTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var creator: RecordingLotteryCreator
    private val data = LotteryUiFixtures

    @Before
    fun resetRepository() {
        creator = RecordingLotteryCreator()
    }

    // US 02.04.01: The owner reaches the creation form from their campsite details.
    @Test
    fun ownerCanOpenCreationFormForSelectedCampsite() {
        showOwnerDetails()
        openForm()
        compose.onNodeWithText("Create lottery").assertIsDisplayed()
        compose.onNodeWithText(data.campsite.name).assertIsDisplayed()
        field("Registration starts").assertExists()
        field("Registration ends").assertExists()
        field("Booking starts").assertExists()
        field("Booking ends").assertExists()
        compose.runOnIdle { assertEquals(0, creator.calls) }
    }

    // US 02.04.01 / .03 / .04 / .05 / .07:
    // Enter all dates and N, leave capacity blank, and publish the linked lottery.
    @Test
    fun publishValidLotteryWithUnlimitedCapacity() {
        showOwnerDetails()
        openForm()
        fillDates()
        replace("Number of winners", "2")
        publish()
        assertPublished()
        compose.runOnIdle {
            val saved = creator.saved.single()
            assertEquals(data.campsite.id, saved.campsiteId)
            assertEquals(data.campsite.name, saved.campsiteName)
            assertEquals(data.OWNER_ID, saved.ownerId)
            assertEquals(data.registrationStart.millis(), saved.registrationStart)
            assertEquals(data.registrationEnd.millis(), saved.registrationEnd)
            assertEquals(data.bookingStart.millis(), saved.bookingStart)
            assertEquals(data.bookingEnd.millis(), saved.bookingEnd)
            assertEquals(2, saved.winnerCount)
            assertNull(saved.waitlistCapacity)
            assertEquals(0, saved.waitingListCount)
            assertEquals(Lottery.STATUS_OPEN, saved.status)
        }
    }

    // US 02.04.07: A capacity equal to N is accepted and passed to persistence.
    @Test
    fun publishAcceptsCapacityEqualToWinnerCount() {
        showOwnerDetails()
        openForm()
        fillDates()
        replace("Number of winners", "2")
        replace("Waiting-list capacity (optional)", "2")
        publish()
        assertPublished()
        compose.runOnIdle { assertEquals(2, creator.saved.single().waitlistCapacity) }
    }

    // US 02.04.07: A larger optional capacity is also accepted.
    @Test
    fun publishAcceptsCapacityAboveWinnerCount() {
        showOwnerDetails()
        openForm()
        fillDates()
        replace("Number of winners", "2")
        replace("Waiting-list capacity (optional)", "10")
        publish()
        assertPublished()
        compose.runOnIdle { assertEquals(10, creator.saved.single().waitlistCapacity) }
    }

    // US 02.04.03: Missing dates produce a visible error without a save.
    @Test
    fun publishWithoutDatesShowsRequiredFieldError() {
        showOwnerDetails()
        openForm()
        publish()
        assertRejected("Choose a registration start.")
    }

    // US 02.04.03: Registration cannot close before it opens.
    @Test
    fun reversedRegistrationPeriodIsRejectedOnScreen() {
        showOwnerDetails()
        openForm()
        fillDates(registrationEnd = UiMoment(2027, 3, 31, 17))
        publish()
        assertRejected("Registration must start before it ends.")
    }

    // US 02.04.03 / .04: Registration cannot overlap the campsite stay period.
    @Test
    fun registrationOverlappingBookingIsRejectedOnScreen() {
        showOwnerDetails()
        openForm()
        fillDates(bookingStart = UiMoment(2027, 4, 7, 15))
        publish()
        assertRejected("Registration must end on or before the booking period starts.")
    }

    // US 02.04.04: A reversed stay period is rejected.
    @Test
    fun reversedBookingPeriodIsRejectedOnScreen() {
        showOwnerDetails()
        openForm()
        fillDates(bookingEnd = UiMoment(2027, 5, 13, 11))
        publish()
        assertRejected("The booking period must start before it ends.")
    }

    // US 02.04.05: The owner must request at least one winner.
    @Test
    fun zeroWinnersIsRejectedOnScreen() {
        showOwnerDetails()
        openForm()
        fillDates()
        replace("Number of winners", "0")
        publish()
        assertRejected("Choose at least one winner.")
    }

    // US 02.04.05: Text input must be a representable whole number.
    @Test
    fun overflowingWinnerCountIsRejectedOnScreen() {
        showOwnerDetails()
        openForm()
        replace("Number of winners", "999999999999999999999")
        publish()
        assertRejected("Enter a whole number of winners.")
    }

    // US 02.04.07: A supplied limit cannot be smaller than N.
    @Test
    fun capacityBelowWinnerCountIsRejectedOnScreen() {
        showOwnerDetails()
        openForm()
        fillDates()
        replace("Number of winners", "3")
        replace("Waiting-list capacity (optional)", "2")
        publish()
        assertRejected("Waiting-list capacity must be at least the number of winners, or left blank.")
    }

    // US 02.04.07: A decimal is not a valid integer capacity.
    @Test
    fun malformedCapacityIsRejectedOnScreen() {
        showOwnerDetails()
        openForm()
        replace("Waiting-list capacity (optional)", "2.5")
        publish()
        assertRejected("Enter a whole-number capacity or leave it blank.")
    }

    // US 02.04.01: A failed save retains input and permits a successful retry.
    @Test
    fun saveFailureKeepsFormValuesAndAllowsRetry() {
        creator.failNextCreate = true
        showOwnerDetails()
        openForm()
        fillDates()
        replace("Number of winners", "2")
        replace("Waiting-list capacity (optional)", "10")
        publish()
        compose.onNodeWithText("Could not publish lottery: Simulated save failure")
            .performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, creator.calls)
            assertEquals(0, creator.saved.size)
        }
        field("Registration starts").assertTextContains(formatDateTime(data.registrationStart.millis()))
        field("Booking ends").assertTextContains(formatDateTime(data.bookingEnd.millis()))
        field("Number of winners").assertTextContains("2")
        field("Waiting-list capacity (optional)").assertTextContains("10")
        publish()
        assertPublished()
        compose.runOnIdle {
            assertEquals(2, creator.calls)
            assertEquals(1, creator.saved.size)
        }
    }

    // US 02.04.01: Publication controls are disabled until the pending save finishes.
    @Test
    fun pendingSaveDisablesPublishAndCancelUntilCompletion() {
        val gate = CompletableDeferred<Unit>()
        creator.gate = gate
        showOwnerDetails()
        openForm()
        fillDates()
        publish()
        try {
            compose.onNodeWithText("Publish").assertIsNotEnabled()
            compose.onNodeWithText("Cancel").assertIsNotEnabled()
            compose.onNodeWithText("Publishing lottery…").performScrollTo().assertIsDisplayed()
            compose.runOnIdle { assertEquals(1, creator.calls) }
        } finally {
            compose.runOnIdle { gate.complete(Unit) }
        }
        assertPublished()
    }

    // US 02.04.01: Cancel returns to details without saving a lottery.
    @Test
    fun cancelReturnsToCampsiteDetailsWithoutSaving() {
        showOwnerDetails()
        openForm()
        compose.onNodeWithText("Cancel").performScrollTo().performClick()
        compose.onNodeWithText("Create lottery").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Publish").assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, creator.calls) }
    }

    // US 02.04.01: Another owner's campsite cannot expose the creation action.
    @Test
    fun differentOwnerCannotOpenCreationForm() {
        showOwnerDetails(activeOwnerId = "another-owner-device")
        compose.onNodeWithText("Campsite not found. It may have been removed.").assertIsDisplayed()
        compose.onNodeWithText("Create lottery").assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, creator.calls) }
    }

    /** Hosts the real destination but supplies a test repository instead of Firebase. */
    private fun showOwnerDetails(activeOwnerId: String = data.OWNER_ID) {
        compose.setContent {
            MaterialTheme {
                OwnerCampsiteDetailsDestination(
                    campsiteId = data.campsite.id,
                    ownerId = activeOwnerId,
                    campsites = listOf(data.campsite),
                    isLoading = false,
                    errorMessage = null,
                    onRetry = {},
                    onBack = {},
                    onPublishLottery = { campsite, settings ->
                        LotteryController.publishLottery(creator, campsite, activeOwnerId, settings)
                    },
                )
            }
        }
    }

    private fun openForm() {
        compose.onNodeWithText("Create lottery").performScrollTo().performClick()
    }

    private fun field(label: String): SemanticsNodeInteraction =
        compose.onNode(hasSetTextAction() and hasText(label))

    private fun replace(label: String, value: String) {
        field(label).performScrollTo().performTextReplacement(value)
    }

    private fun publish() {
        compose.onNodeWithText("Publish").performScrollTo().performClick()
    }

    private fun assertPublished() {
        val message = "Lottery published. View it in the Lotteries tab."
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(message).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Publish").assertDoesNotExist()
    }

    private fun assertRejected(message: String) {
        compose.onNodeWithText(message).performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(0, creator.calls)
            assertEquals(0, creator.saved.size)
        }
        compose.onNodeWithText("Publish").assertExists()
    }

    private fun fillDates(
        registrationStart: UiMoment = data.registrationStart,
        registrationEnd: UiMoment = data.registrationEnd,
        bookingStart: UiMoment = data.bookingStart,
        bookingEnd: UiMoment = data.bookingEnd,
    ) {
        chooseDateTime(0, registrationStart)
        chooseDateTime(1, registrationEnd)
        chooseDateTime(2, bookingStart)
        chooseDateTime(3, bookingEnd)
    }

    /**
     * The four Choose buttons are in registration-start/end, booking-start/end order.
     * Compose opens the dialog; Espresso controls the native Android widgets.
     * PickerActions.setDate uses a 1-based month, unlike Calendar's 0-based month.
     */
    private fun chooseDateTime(index: Int, moment: UiMoment) {
        compose.onAllNodesWithText("Choose", useUnmergedTree = true)[index]
            .performScrollTo().performClick()
        onView(isAssignableFrom(DatePicker::class.java))
            .perform(PickerActions.setDate(moment.year, moment.month, moment.day))
        onView(withId(android.R.id.button1)).perform(click())
        onView(isAssignableFrom(TimePicker::class.java))
            .perform(PickerActions.setTime(moment.hour, moment.minute))
        onView(withId(android.R.id.button1)).perform(click())
        compose.waitForIdle()
    }
}
