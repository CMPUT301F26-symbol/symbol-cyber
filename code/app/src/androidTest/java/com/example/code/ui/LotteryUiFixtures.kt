package com.example.code.ui
import com.example.code.controller.LotteryController
import com.example.code.data.JoinResult
import com.example.code.data.LotteryCreationRepository
import com.example.code.data.LotteryRepository
import com.example.code.data.NotificationRepository
import com.example.code.model.AppNotification
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.WaitingListEntry
import java.util.Calendar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** AI USAGE
 * The basis for the following UI intent test bench file was generated using
 * GPT6-Sol "Please create a basic set of UI intent tests for my lottery files", 11-05-2026
 *
 */

/** Realistic local dates; conversion uses the same device zone as the form. */
internal data class UiMoment(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int = 0,
) {
    fun millis(): Long = Calendar.getInstance().apply {
        clear()
        isLenient = false
        set(year, month - 1, day, hour, minute, 0)
    }.timeInMillis
}

/** Fixture data belongs in androidTest; src/test fixtures are a separate source set. */
internal object LotteryUiFixtures {
    const val OWNER_ID = "owner-device-pine-creek"
    val campsite = Campsite(
        id = "campsite-pine-creek-17",
        ownerId = OWNER_ID,
        name = "Pine Creek Campground",
        location = "Hinton, Alberta",
        description = "Wooded campsite with a cookhouse and picnic table.",
        maxOccupancy = 6,
        latitude = 53.399,
        longitude = -117.581,
        price = 65.0,
        currency = "CAD",
        rules = "Quiet hours begin at 10 p.m.",
        cancellationPolicy = "Cancel at least 48 hours before arrival.",
    )

    val registrationStart = UiMoment(2027, 4, 1, 9)
    val registrationEnd = UiMoment(2027, 4, 8, 17)
    val bookingStart = UiMoment(2027, 5, 14, 15)
    val bookingEnd = UiMoment(2027, 5, 16, 11)

    val lottery = Lottery(
        id = "lottery-pine-creek-may-2027",
        campsiteId = campsite.id,
        campsiteName = campsite.name,
        ownerId = OWNER_ID,
        registrationStart = registrationStart.millis(),
        registrationEnd = registrationEnd.millis(),
        bookingStart = bookingStart.millis(),
        bookingEnd = bookingEnd.millis(),
        winnerCount = 2,
        waitlistCapacity = 10,
        waitingListCount = 3,
    )

    val entrants = listOf(
        WaitingListEntry("entrant-priya-01", "Priya Shah", registrationStart.millis() + 60_000),
        WaitingListEntry("entrant-liam-02", "Liam Chen", registrationStart.millis() + 120_000),
        WaitingListEntry("entrant-sofia-03", "Sofia Martins", registrationStart.millis() + 180_000),
    )
}

/**
 * Test-only creation repository. Records controller output without calling Firebase.
 * A gate allows a UI test to keep a save pending; failure allows retry testing.
 */
internal class RecordingLotteryCreator : LotteryCreationRepository {
    val saved = mutableListOf<Lottery>()
    var calls = 0
    var failNextCreate = false
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun createLottery(lottery: Lottery): String {
        calls++
        gate?.await()
        if (failNextCreate) {
            failNextCreate = false
            throw IllegalStateException("Simulated save failure")
        }
        saved += lottery
        return "created-lottery-${saved.size}"
    }
}

/** In-memory state for instrumented draw tests, isolated from src/test fakes. */
internal class LotteryDrawUiRepository(
    initial: Lottery,
    entries: List<WaitingListEntry>,
) : LotteryRepository {
    val lottery = MutableStateFlow(initial)
    val waitingList = MutableStateFlow(entries)
    var completedDraws = 0
        private set

    override fun observeLotteries(): Flow<List<Lottery>> = lottery.map { listOf(it) }

    override fun observeWaitingList(lotteryId: String): Flow<List<WaitingListEntry>> =
        waitingList

    override suspend fun getWaitingList(lotteryId: String): List<WaitingListEntry> =
        waitingList.value

    override suspend fun joinWaitingList(lotteryId: String, entry: WaitingListEntry): JoinResult {
        val current = lottery.value
        if (waitingList.value.any { it.entrantId == entry.entrantId }) return JoinResult.ALREADY_JOINED
        if (!LotteryController.hasRoom(current)) return JoinResult.FULL
        waitingList.value = waitingList.value + entry
        lottery.value = current.copy(waitingListCount = waitingList.value.size)
        return JoinResult.JOINED
    }

    override suspend fun leaveWaitingList(lotteryId: String, entrantId: String): Boolean {
        if (waitingList.value.none { it.entrantId == entrantId }) return false
        waitingList.value = waitingList.value.filterNot { it.entrantId == entrantId }
        lottery.value = lottery.value.copy(waitingListCount = waitingList.value.size)
        return true
    }

    override suspend fun completeDraw(
        lotteryId: String,
        winnerIds: List<String>,
        notSelectedIds: List<String>,
        drawnAt: Long,
    ): Boolean {
        if (lottery.value.status != Lottery.STATUS_OPEN) return false
        completedDraws++
        waitingList.value = waitingList.value.map { entry ->
            when (entry.entrantId) {
                in winnerIds -> entry.copy(status = WaitingListEntry.STATUS_WON)
                in notSelectedIds -> entry.copy(status = WaitingListEntry.STATUS_NOT_SELECTED)
                else -> entry
            }
        }
        lottery.value = lottery.value.copy(status = Lottery.STATUS_DRAWN)
        return true
    }
}

/** Records notifications generated by the actual DrawController. */
internal class RecordingDrawNotifications : NotificationRepository {
    val sent = mutableListOf<AppNotification>()

    override suspend fun sendAll(notifications: List<AppNotification>) {
        sent += notifications
    }
}
