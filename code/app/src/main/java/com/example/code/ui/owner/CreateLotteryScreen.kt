package com.example.code.ui.owner
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.code.controller.LotteryController
import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.ui.common.formatDateTime
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch


/** AI USAGE
 * The basis code for the following file, CreateLotteryScreen by GPT6-Sol
 * "Based on the required project specifications provided and the existing code,
 * I need help adding in code for US 2.04.01, 03,04, 05, and 07. Ensure that the new added features for the lottery
 * do not interfere with the reservation, waitingList, etc.
 * Also, understand from the specifications that many other classes will also depend on new features of the Lottery.
 * Currently, there is a model class, controller, and a LOtteryRepository", 11-05-2026
 */

/**
 * Owner form for one saved campsite. Dates are chosen in the device time zone
 * and submitted as epoch milliseconds; campsite identity is never editable.
 * [onPublish] must return only after persistence succeeds. A failed save keeps
 * the form open with its values, while [onPublished] closes it on success.
 */
@Composable
fun CreateLotteryScreen(
    campsite: Campsite,
    onPublish: suspend (Lottery) -> String,
    onPublished: () -> Unit,
    onCancel: () -> Unit,
) {
    var registrationStart by rememberSaveable(campsite.id) { mutableStateOf<Long?>(null) }
    var registrationEnd by rememberSaveable(campsite.id) { mutableStateOf<Long?>(null) }
    var bookingStart by rememberSaveable(campsite.id) { mutableStateOf<Long?>(null) }
    var bookingEnd by rememberSaveable(campsite.id) { mutableStateOf<Long?>(null) }
    var winners by rememberSaveable(campsite.id) { mutableStateOf("1") }
    var capacity by rememberSaveable(campsite.id) { mutableStateOf("") }
    var error by rememberSaveable(campsite.id) { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        if (!busy) onCancel()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Create lottery", style = MaterialTheme.typography.headlineSmall)
        Text(campsite.name, style = MaterialTheme.typography.titleMedium)
        Text("All dates and times use ${TimeZone.getDefault().id}.")

        Text("Registration period", style = MaterialTheme.typography.titleMedium)
        Text("Entrants can join from the start time until registration closes.")
        LotteryDateTimeField("Registration starts", registrationStart, !busy) { registrationStart = it }
        LotteryDateTimeField("Registration ends", registrationEnd, !busy) { registrationEnd = it }

        Text("Booking period", style = MaterialTheme.typography.titleMedium)
        Text("The stay dates offered to lottery winners.")
        LotteryDateTimeField("Booking starts", bookingStart, !busy) { bookingStart = it }
        LotteryDateTimeField("Booking ends", bookingEnd, !busy) { bookingEnd = it }

        OutlinedTextField(
            value = winners,
            onValueChange = { winners = it },
            label = { Text("Number of winners") },
            supportingText = { Text("At least 1. If fewer entrants join, all are selected.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = capacity,
            onValueChange = { capacity = it },
            label = { Text("Waiting-list capacity (optional)") },
            supportingText = { Text("Leave blank for unlimited; otherwise at least the number of winners.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (busy) {
            CircularProgressIndicator()
            Text("Publishing lottery…")
        }
        Button(
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // Parse and validate before launching a write. Blank capacity is
                // unlimited; malformed or overflowing integers are errors.
                val draft = try {
                    val winnerCount = requireNotNull(winners.trim().toIntOrNull()) {
                        "Enter a whole number of winners."
                    }
                    val cap = if (capacity.isBlank()) null else {
                        requireNotNull(capacity.trim().toIntOrNull()) {
                            "Enter a whole-number capacity or leave it blank."
                        }
                    }
                    Lottery(
                        campsiteId = campsite.id,
                        campsiteName = campsite.name,
                        ownerId = campsite.ownerId,
                        registrationStart = requireNotNull(registrationStart) { "Choose a registration start." },
                        registrationEnd = requireNotNull(registrationEnd) { "Choose a registration end." },
                        bookingStart = requireNotNull(bookingStart) { "Choose a booking start." },
                        bookingEnd = requireNotNull(bookingEnd) { "Choose a booking end." },
                        winnerCount = winnerCount,
                        waitlistCapacity = cap,
                    ).also(LotteryController::validateCreation)
                } catch (e: IllegalArgumentException) {
                    error = e.message
                    null
                }
                if (draft != null && !busy) {
                    busy = true
                    error = null
                    scope.launch {
                        try {
                            onPublish(draft)
                            onPublished()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            error = "Could not publish lottery: ${e.message ?: "Please try again."}"
                        } finally {
                            busy = false
                        }
                    }
                }
            },
        ) { Text("Publish") }
        TextButton(onClick = onCancel, enabled = !busy) { Text("Cancel") }
    }
}

/** Native pickers avoid requiring an additional date library or Material opt-in. */
@Composable
private fun LotteryDateTimeField(
    label: String,
    value: Long?,
    enabled: Boolean,
    onSelected: (Long) -> Unit,
) {
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<Dialog?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        onDispose { dialog?.dismiss() }
    }
    OutlinedTextField(
        value = value?.let(::formatDateTime).orEmpty(),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            TextButton(
                enabled = enabled,
                onClick = {
                    val initial = Calendar.getInstance().apply {
                        if (value != null) timeInMillis = value
                    }
                    val dateDialog = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val timeDialog = TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val selected = Calendar.getInstance().apply {
                                        isLenient = false
                                        clear()
                                        set(year, month, day, hour, minute, 0)
                                    }
                                    try {
                                        onSelected(selected.timeInMillis)
                                        error = null
                                    } catch (e: IllegalArgumentException) {
                                        error = "That local time does not exist. Choose another time."
                                    }
                                },
                                initial.get(Calendar.HOUR_OF_DAY),
                                initial.get(Calendar.MINUTE),
                                DateFormat.is24HourFormat(context),
                            )
                            timeDialog.setTitle(label)
                            dialog = timeDialog
                            timeDialog.show()
                        },
                        initial.get(Calendar.YEAR),
                        initial.get(Calendar.MONTH),
                        initial.get(Calendar.DAY_OF_MONTH),
                    )
                    dateDialog.setTitle(label)
                    dialog = dateDialog
                    dateDialog.show()
                },
            ) { Text("Choose") }
        },
    )
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
