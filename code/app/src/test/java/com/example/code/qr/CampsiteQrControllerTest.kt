/* Tests stable, distinct promotional codes and actual ZXing decode round trips. Known issues: none. */
package com.example.code.qr

import com.example.code.model.Campsite
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.*
import org.junit.Test

class CampsiteQrControllerTest {
    private val controller = CampsiteQrController()
    private val site = Campsite(id = "jasper-riverside-2027", ownerId = "owner-dean", name = "Jasper Riverside")

    @Test fun generatedImageDecodesToExactlyTheSavedCampsite() {
        val image = controller.generate(site, site.ownerId)
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(image.width, image.width, image.pixels)))
        val decoded = QRCodeReader().decode(bitmap).text
        assertEquals(site.id, CampsiteQrLink.decode(decoded))
        assertEquals(image.payload, decoded)
        assertEquals(0xffffffff.toInt(), image.pixels.first())
    }

    @Test fun nameChangesDoNotInvalidateCodeAndDifferentIdsHaveDifferentCodes() {
        val first = controller.generate(site, site.ownerId)
        val edited = controller.generate(site.copy(name = "Renamed campsite"), site.ownerId)
        assertEquals(first.payload, edited.payload)
        assertArrayEquals(first.pixels, edited.pixels)
        assertNotEquals(first.payload, controller.generate(site.copy(id = "banff-pines-2027"), site.ownerId).payload)
    }

    @Test fun longIdsAreNotCroppedToASmallRequestedCanvas() {
        val image = controller.generate(site.copy(id = "a".repeat(1500)), site.ownerId, 128)
        assertTrue(image.width >= 128)
        assertEquals(image.width * image.width, image.pixels.size)
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(image.width, image.width, image.pixels)))
        assertEquals(image.payload, QRCodeReader().decode(bitmap).text)
    }

    @Test fun supportsEncodedDocumentIds() {
        val id = "Campsite été % north"
        assertEquals(id, CampsiteQrLink.decode(CampsiteQrLink.encode(id)))
    }

    @Test fun rejectsForeignMalformedAndPathTraversalPayloads() {
        for (text in listOf("", "https://example.com/camp", "symbol://campsite/v2/jasper", "symbol://campsite/v1/%2F", "symbol://campsite/v1/%2E%2E", "symbol://campsite/v1/jasper?redirect=evil", "symbol://other/v1/jasper", "symbol://campsite/v1/%ZZ")) {
            assertNull(text, CampsiteQrLink.decode(text))
        }
    }

    @Test fun refusesUnsavedCampsitesAndOtherOwners() {
        for (pair in listOf(site.copy(id = "") to site.ownerId, site to "someone-else", site.copy(ownerId = "") to "")) {
            try { controller.generate(pair.first, pair.second); fail("Invalid QR request accepted") }
            catch (_: IllegalArgumentException) { }
        }
    }
}
