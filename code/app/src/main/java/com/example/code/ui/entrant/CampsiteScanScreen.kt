/*
 * CampsiteScanScreen.kt
 * Opens a QR-only camera scanner or accepts a campsite link and resolves it before navigation.
 * Known issues: TA library approval (#95) and a physical camera/poster smoke check remain required.
 */
package com.example.code.ui.entrant

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.code.qr.CampsiteScanController
import com.example.code.qr.CampsiteScanResult
import com.example.code.qr.FirestoreCampsiteLookup
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

/**
 * Scans and resolves a campsite before calling [onOpenCampsite].
 * [scanner] can replace camera hardware with a decoded-result callback for acceptance tests.
 */
@Composable
fun CampsiteScanRoute(
    onOpenCampsite: (String) -> Unit,
    controller: CampsiteScanController = remember { CampsiteScanController(FirestoreCampsiteLookup()) },
    scanner: (((String?) -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var link by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val onDecoded: (String?) -> Unit = { payload ->
        if (!busy) {
            if (payload == null) message = "Scanning cancelled. You can try again."
            else {
                busy = true
                message = null
                scope.launch {
                    val result = controller.resolve(payload)
                    busy = false
                    when (result) {
                        is CampsiteScanResult.Found -> onOpenCampsite(result.campsiteId)
                        CampsiteScanResult.Invalid -> message = "This is not a valid Symbol campsite code."
                        CampsiteScanResult.Missing -> message = "This campsite does not exist or has been removed."
                        CampsiteScanResult.Unavailable -> message = "Could not check this campsite. Check your connection and try again."
                    }
                }
            }
        }
    }
    val camera = rememberLauncherForActivityResult(ScanContract()) { onDecoded(it.contents) }
    val openCamera = {
        try {
            camera.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Scan a Symbol campsite QR code").setBeepEnabled(false).setOrientationLocked(false))
        } catch (_: RuntimeException) {
            message = "Camera scanning is unavailable. You can paste a campsite link instead."
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openCamera()
        else message = "Camera permission is needed to scan. Allow Camera in system settings, or paste a campsite link."
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Scan campsite QR", style = MaterialTheme.typography.headlineSmall)
        Text("Scan a Symbol campsite code to open its details and active lottery. You can also paste a campsite link.")
        Button(
            enabled = !busy,
            onClick = {
                message = null
                when {
                    scanner != null -> scanner(onDecoded)
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openCamera()
                    else -> permission.launch(Manifest.permission.CAMERA)
                }
            },
        ) { Text("Scan QR code") }
        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text("Paste campsite link") },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = { onDecoded(link) }, enabled = !busy && link.isNotBlank()) { Text("Open campsite link") }
        if (busy) { CircularProgressIndicator(); Text("Checking campsite…") }
        message?.let { Text(it) }
    }
}
