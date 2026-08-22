package com.cielotickets.app.data.repository

import com.cielotickets.app.data.local.dao.TicketDao
import com.cielotickets.app.data.mapper.toDomain
import com.cielotickets.app.data.mapper.toEntity
import com.cielotickets.app.data.mapper.toTicketDomainList
import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val ticketDao: TicketDao
) : TicketRepository {
    override suspend fun saveTicket(ticket: Ticket) {
        ticketDao.insert(ticket.toEntity())
    }

    override fun getTicketById(id: String): Flow<Ticket?> {
        return ticketDao.getById(id).map { it?.toDomain() }
    }

    override fun getAllTickets(): Flow<List<Ticket>> {
        return ticketDao.getAll().map { it.toTicketDomainList() }
    }

    override suspend fun getTicketByReference(reference: String): Ticket? {
        return ticketDao.getByPurchaseReference(reference)?.toDomain()
    }
}
