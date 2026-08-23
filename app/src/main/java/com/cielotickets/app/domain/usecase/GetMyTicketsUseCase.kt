package com.cielotickets.app.domain.usecase

import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.domain.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyTicketsUseCase @Inject constructor(private val ticketRepository: TicketRepository) {
    operator fun invoke(): Flow<List<Ticket>> = ticketRepository.getAllTickets()
}
