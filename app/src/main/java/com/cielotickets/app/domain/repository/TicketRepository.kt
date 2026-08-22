package com.cielotickets.app.domain.repository

import com.cielotickets.app.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    suspend fun saveTicket(ticket: Ticket)
    fun getTicketById(id: String): Flow<Ticket?>
    fun getAllTickets(): Flow<List<Ticket>>
    suspend fun getTicketByReference(reference: String): Ticket?
}
