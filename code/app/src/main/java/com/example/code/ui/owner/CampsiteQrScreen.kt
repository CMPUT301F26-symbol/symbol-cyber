/*
 * CampsiteQrScreen.kt
 * Displays a saved campsite's promotional QR code and supports copying its link.
 * Known issues: library approval under #95 is required before production adoption.
 */
package com.example.code.ui.owner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.code.model.Campsite
import com.example.code.qr.CampsiteQrController
import com.example.code.qr.CampsiteQrPixels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Displays a code for [campsite]; [onCopyLink] can replace the system clipboard in tests. */
@Composable
fun CampsiteQrScreen(
    campsite: Campsite,
    ownerId: String,
    onBack: () -> Unit,
    onCopyLink: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    var attempt by remember { mutableStateOf(0) }
    var copied by remember(campsite.id) { mutableStateOf(false) }
    val result by produceState<Result<CampsiteQrPixels>?>(null, campsite.id, ownerId, attempt) {
        value = null
        value = withContext(Dispatchers.Default) {
            runCatching { CampsiteQrController().generate(campsite, ownerId) }
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text("Promotional QR code", style = MaterialTheme.typography.headlineSmall)
        Text(campsite.name)
        val image = result?.getOrNull()
        when {
            result == null -> CircularProgressIndicator()
            image == null -> {
                Text("Could not generate this code. Check that the campsite is saved and belongs to you.")
                Button(onClick = { attempt++ }) { Text("Retry") }
            }
            else -> {
                val bitmap = remember(image) {
                    Bitmap.createBitmap(image.pixels, image.width, image.width, Bitmap.Config.ARGB_8888).asImageBitmap()
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = "Promotional QR code for ${campsite.name}",
                    modifier = Modifier.fillMaxWidth().sizeIn(maxWidth = 360.dp, maxHeight = 360.dp),
                )
                Text("Scan this code in Symbol to open this campsite. The code stays the same if you update the campsite details.")
                Button(onClick = {
                    if (onCopyLink != null) onCopyLink(image.payload)
                    else (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("Campsite link", image.payload))
                    copied = true
                }) { Text("Copy campsite link") }
                if (copied) Text("Campsite link copied")
            }
        }
    }
}
