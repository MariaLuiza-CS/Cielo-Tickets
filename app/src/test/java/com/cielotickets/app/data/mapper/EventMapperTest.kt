package com.cielotickets.app.data.mapper

import com.cielotickets.app.data.remote.model.EventDto
import com.cielotickets.app.domain.model.Ticket
import org.junit.Assert.assertEquals
import org.junit.Test

class EventMapperTest {

    @Test
    fun `EventDto toEntity should preserve values`() {
        val dto = EventDto(
            id = "1",
            name = "Event",
            date = "2024",
            location = "Loc",
            price = 100.0,
            imageUrl = "url",
            availableTickets = 10,
        )
        val entity = dto.toEntity()
        assertEquals(dto.id, entity.id)
        assertEquals(dto.name, entity.name)
        assertEquals(dto.price, entity.price, 0.0)
    }

    @Test
    fun `Ticket toEntity and back should preserve values`() {
        val domain = Ticket(
            ticketId = "t1",
            eventId = "e1",
            eventName = "Event",
            purchaseReference = "ref",
            cieloOrderId = "c1",
            qrPayload = "qr",
            createdAt = 12345L,
        )
        val entity = domain.toEntity()
        val backToDomain = entity.toDomain()

        assertEquals(domain, backToDomain)
    }
}
