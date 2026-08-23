package com.cielotickets.app.domain.usecase

import app.cash.turbine.test
import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.domain.repository.EventRepository
import com.cielotickets.app.domain.repository.TicketRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class UseCaseTests {

    @Test
    fun `GetEventsUseCase should delegate to repository`() = runTest {
        val repo = mockk<EventRepository>()
        val useCase = GetEventsUseCase(repo)
        val events = listOf(mockk<Event>())
        coEvery { repo.getEvents() } returns Result.success(events)

        val result = useCase()
        assertEquals(events, result.getOrNull())
    }

    @Test
    fun `GetEventByIdUseCase should delegate to repository`() = runTest {
        val repo = mockk<EventRepository>()
        val useCase = GetEventByIdUseCase(repo)
        val event = mockk<Event>()
        coEvery { repo.getEventById("1") } returns Result.success(event)

        val result = useCase("1")
        assertEquals(event, result.getOrNull())
    }

    @Test
    fun `CreateTicketUseCase should generate valid ticket and save`() = runTest {
        val repo = mockk<TicketRepository>(relaxed = true)
        val useCase = CreateTicketUseCase(repo)

        val ticket = useCase("e1", "Event Name", "ref", "cielo123")

        assertEquals("e1", ticket.eventId)
        assertEquals("Event Name", ticket.eventName)
        assertEquals("ref", ticket.purchaseReference)
        assertEquals("cielo123", ticket.cieloOrderId)
        assertTrue(ticket.qrPayload.contains("CIELOTICKETS"))
        // ticketId should be a valid UUID
        UUID.fromString(ticket.ticketId)

        coVerify { repo.saveTicket(any()) }
    }

    @Test
    fun `GetMyTicketsUseCase should return flow from repository`() = runTest {
        val repo = mockk<TicketRepository>()
        val useCase = GetMyTicketsUseCase(repo)
        val tickets = listOf(mockk<Ticket>())
        coEvery { repo.getAllTickets() } returns flowOf(tickets)

        useCase().test {
            assertEquals(tickets, awaitItem())
            awaitComplete()
        }
    }
}
