package com.cielotickets.app.domain.usecase

import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.domain.repository.TicketRepository
import java.util.UUID
import javax.inject.Inject

class CreateTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(
        eventId: String,
        eventName: String,
        purchaseReference: String,
        cieloOrderId: String?
    ): Ticket {
        val ticketId = UUID.randomUUID().toString()
        val qrPayload = "CIELOTICKETS|$ticketId|$cieloOrderId"
        
        val ticket = Ticket(
            ticketId = ticketId,
            eventId = eventId,
            eventName = eventName,
            purchaseReference = purchaseReference,
            cieloOrderId = cieloOrderId,
            qrPayload = qrPayload,
            createdAt = System.currentTimeMillis()
        )
        
        ticketRepository.saveTicket(ticket)
        return ticket
    }
}
