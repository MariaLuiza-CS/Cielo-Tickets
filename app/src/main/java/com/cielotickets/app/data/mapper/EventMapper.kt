package com.cielotickets.app.data.mapper

import com.cielotickets.app.data.local.entity.EventEntity
import com.cielotickets.app.data.local.entity.TicketEntity
import com.cielotickets.app.data.remote.model.EventDto
import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.model.Ticket

fun EventDto.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        name = name,
        date = date,
        location = location,
        price = price,
        imageUrl = imageUrl,
        availableTickets = availableTickets
    )
}

fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        name = name,
        date = date,
        location = location,
        price = price,
        imageUrl = imageUrl,
        availableTickets = availableTickets
    )
}

fun List<EventEntity>.toDomainList(): List<Event> = map { it.toDomain() }
fun List<EventDto>.toEntityList(): List<EventEntity> = map { it.toEntity() }

fun Ticket.toEntity(): TicketEntity {
    return TicketEntity(
        ticketId = ticketId,
        eventId = eventId,
        eventName = eventName,
        purchaseReference = purchaseReference,
        cieloOrderId = cieloOrderId,
        qrPayload = qrPayload,
        createdAt = createdAt
    )
}

fun TicketEntity.toDomain(): Ticket {
    return Ticket(
        ticketId = ticketId,
        eventId = eventId,
        eventName = eventName,
        purchaseReference = purchaseReference,
        cieloOrderId = cieloOrderId,
        qrPayload = qrPayload,
        createdAt = createdAt
    )
}

fun List<TicketEntity>.toTicketDomainList(): List<Ticket> = map { it.toDomain() }
