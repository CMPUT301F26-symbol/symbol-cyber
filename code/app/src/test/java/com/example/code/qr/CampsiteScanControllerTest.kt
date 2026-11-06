/* Tests exact QR target resolution, bad/removed codes, read failure and cancellation. Known issues: none. */
package com.example.code.qr

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CampsiteScanControllerTest {
    @Test fun validLinkChecksTheExactCampsiteBeforeOpening() = runBlocking {
        var requested: String? = null
        val controller = CampsiteScanController { id -> requested = id; id == "jasper-riverside-2027" }
        assertEquals(CampsiteScanResult.Found("jasper-riverside-2027"), controller.resolve(CampsiteQrLink.encode("jasper-riverside-2027")))
        assertEquals("jasper-riverside-2027", requested)
    }

    @Test fun malformedAndForeignPayloadsNeverReadFirebase() = runBlocking {
        var requests = 0
        val controller = CampsiteScanController { requests++; true }
        for (text in listOf("", "https://example.com/campsite", "symbol://campsite/v2/jasper", "symbol://campsite/v1/%2F", "symbol://campsite/v1/jasper#redirect")) {
            assertEquals(CampsiteScanResult.Invalid, controller.resolve(text))
        }
        assertEquals(0, requests)
    }

    @Test fun removedTargetsAndReadFailuresNeverBecomeFound() = runBlocking {
        val link = CampsiteQrLink.encode("jasper-riverside-2027")
        assertEquals(CampsiteScanResult.Missing, CampsiteScanController { false }.resolve(link))
        assertEquals(CampsiteScanResult.Unavailable, CampsiteScanController { throw IllegalStateException("offline") }.resolve(link))
    }

    @Test fun cancellationIsNotPresentedAsANetworkError() = runBlocking {
        try {
            CampsiteScanController { throw CancellationException("leaving screen") }.resolve(CampsiteQrLink.encode("jasper-riverside-2027"))
            fail("Cancellation was swallowed")
        } catch (_: CancellationException) { }
    }
}
