/* Tests combined browse criteria, inclusive date boundaries, booking changes and invalid input. Known issues: none. */
package com.example.code.controller

import com.example.code.model.Campsite
import com.example.code.model.Lottery
import com.example.code.model.Reservation
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class CampsiteFilterControllerTest {
    private val controller = CampsiteFilterController(TimeZone.getTimeZone("America/Edmonton"))
    private val sites = listOf(
        Campsite(id = "jasper", name = "Jasper Riverside", location = "Jasper", maxOccupancy = 6),
        Campsite(id = "banff", name = "Banff Pines", location = "Banff", maxOccupancy = 2),
    )
    private val stay = controller.parse("", "", "2027-05-01", "2027-05-31").stay!!
    private val lotteries = sites.map { Lottery(campsiteId = it.id, bookingStart = stay.start, bookingEnd = stay.end) }

    @Test fun combinesEveryCriterionAndClearRestoresAll() {
        val selection = controller.parse(" JASPER ", "4", "2027-05-14", "2027-05-16")
        assertEquals(listOf(sites[0]), controller.apply(sites, selection, lotteries, emptyList()))
        assertEquals(sites, controller.apply(sites, CampsiteFilterSelection(), emptyList(), emptyList()))
        assertTrue(controller.apply(sites, controller.parse("", "20", "", ""), lotteries, emptyList()).isEmpty())
    }

    @Test fun inclusiveDatesExcludeBookingsAndReactToRejection() {
        val selection = controller.parse("Jasper", "", "2027-05-01", "2027-05-31")
        assertEquals(listOf(sites[0]), controller.apply(sites, selection, lotteries, emptyList()))
        val booking = Reservation(campsiteId = "jasper", startDate = stay.end, endDate = stay.end)
        assertTrue(controller.apply(sites, selection, lotteries, listOf(booking)).isEmpty())
        assertEquals(listOf(sites[0]), controller.apply(sites, selection, lotteries, listOf(booking.copy(status = Reservation.STATUS_REJECTED))))
    }

    @Test fun missingOrPartialAvailabilityDoesNotInventDates() {
        val selection = controller.parse("", "", "2027-05-01", "2027-06-01")
        assertTrue(controller.apply(sites, selection, lotteries, emptyList()).isEmpty())
        assertTrue(controller.apply(sites, selection, emptyList(), emptyList()).isEmpty())
    }

    @Test fun rejectsInvalidInputIncludingOverflowAndImpossibleDates() {
        for (input in listOf(
            listOf("0", "", ""), listOf("-2", "", ""), listOf("1.5", "", ""),
            listOf("999999999999", "", ""), listOf("", "2027-02-30", "2027-03-01"),
            listOf("", "2027-05-01", ""), listOf("", "2027-05-02", "2027-05-01"),
        )) {
            try { controller.parse("", input[0], input[1], input[2]); fail("Invalid input accepted: $input") }
            catch (_: IllegalArgumentException) { }
        }
    }
}
