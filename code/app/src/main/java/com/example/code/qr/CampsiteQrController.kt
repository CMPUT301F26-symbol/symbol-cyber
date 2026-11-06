/*
 * CampsiteQrController.kt
 * Creates printable black/white QR pixels for a saved campsite owned by the caller.
 * Known issues: TA approval for ZXing must be recorded under #95 before production adoption.
 */
package com.example.code.qr

import com.example.code.model.Campsite
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Rendered QR pixels, independent of Android Bitmap so encoding can be tested on the JVM.
 * @property payload the canonical campsite link represented by the image.
 * @property width square image width/height in pixels.
 * @property pixels row-major opaque ARGB pixels, including the quiet border.
 */
data class CampsiteQrPixels(val payload: String, val width: Int, val pixels: IntArray)

/** Generates QR codes without changing the campsite or creating duplicate database records. */
class CampsiteQrController {
    /**
     * Encodes [campsite]'s ID for its [ownerId] into a square image.
     * @throws IllegalArgumentException for an unsaved/unowned campsite or an invalid size.
     * @throws com.google.zxing.WriterException when a payload cannot fit in a QR code.
     */
    fun generate(campsite: Campsite, ownerId: String, size: Int = 512): CampsiteQrPixels {
        require(ownerId.isNotBlank() && campsite.ownerId == ownerId) { "Only this campsite's owner can view its promotional code." }
        require(size in 128..1024) { "QR size must be between 128 and 1024 pixels." }
        val payload = CampsiteQrLink.encode(campsite.id)
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 4,
        )
        val writer = QRCodeWriter()
        val first = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
        // A long payload may need more modules than the requested canvas. Keep modules readable.
        val matrix = if (first.width > size) {
            writer.encode(payload, BarcodeFormat.QR_CODE, first.width * 3, first.width * 3, hints)
        } else first
        val width = matrix.width
        val pixels = IntArray(width * width) { index -> if (matrix[index % width, index / width]) 0xff000000.toInt() else 0xffffffff.toInt() }
        return CampsiteQrPixels(payload, width, pixels)
    }
}
