/*
 * CampsiteScanScreenTest.kt
 * Exercises scan-result callbacks, link fallback, cancellation and errors without real Firebase writes.
 * Known issues: hardware camera permission/decoding needs a physical-device smoke check.
 */
package com.example.code.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.code.qr.CampsiteQrLink
import com.example.code.qr.CampsiteScanController
import com.example.code.ui.entrant.CampsiteScanRoute
import com.journeyapps.barcodescanner.ScanContract
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CampsiteScanScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cancelledScanCanRetryAndOpenOnlyTheResolvedId() {
        val payload = mutableStateOf<String?>(null)
        var opened: String? = null
        compose.setContent {
            MaterialTheme {
                CampsiteScanRoute(
                    onOpenCampsite = { opened = it },
                    controller = CampsiteScanController { it == "jasper-riverside-2027" },
                    scanner = { callback -> callback(payload.value) },
                )
            }
        }
        compose.onNodeWithText("Scan QR code").performClick()
        compose.onNodeWithText("Scanning cancelled. You can try again.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertNull(opened); payload.value = CampsiteQrLink.encode("jasper-riverside-2027") }
        compose.onNodeWithText("Scan QR code").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("jasper-riverside-2027", opened) }
    }

    @Test fun badUnknownAndUnavailableLinksStayOnTheScanScreen() {
        var opened = false
        compose.setContent {
            MaterialTheme {
                CampsiteScanRoute(
                    onOpenCampsite = { opened = true },
                    controller = CampsiteScanController { if (it == "offline") error("offline") else false },
                    scanner = { callback -> callback(null) },
                )
            }
        }
        submit("https://example.com/foreign")
        compose.onNodeWithText("This is not a valid Symbol campsite code.").performScrollTo().assertIsDisplayed()
        submit(CampsiteQrLink.encode("removed-campsite"))
        compose.onNodeWithText("This campsite does not exist or has been removed.").performScrollTo().assertIsDisplayed()
        submit(CampsiteQrLink.encode("offline"))
        compose.onNodeWithText("Could not check this campsite. Check your connection and try again.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { assertFalse(opened) }
    }

    @Test fun nativeScannerContractReturnsTheUnmodifiedPayload() {
        val payload = CampsiteQrLink.encode("jasper-riverside-2027")
        val result = ScanContract().parseResult(Activity.RESULT_OK, Intent()
            .putExtra("SCAN_RESULT", payload).putExtra("SCAN_RESULT_FORMAT", "QR_CODE"))
        assertEquals(payload, result.contents)
        assertNull(ScanContract().parseResult(Activity.RESULT_CANCELED, null).contents)
    }

    private fun submit(link: String) {
        compose.onNodeWithText("Paste campsite link").performScrollTo().performTextReplacement(link)
        compose.onNodeWithText("Open campsite link").performScrollTo().performClick()
    }
}
