package com.cielotickets.app.data.repository

import com.cielotickets.app.data.local.dao.TicketDao
import com.cielotickets.app.data.local.entity.TicketEntity
import com.cielotickets.app.domain.model.Ticket
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TicketRepositoryImplTest {

    private val ticketDao = mockk<TicketDao>(relaxed = true)
    private val repository = TicketRepositoryImpl(ticketDao)

    @Test
    fun `saveTicket should delegate to DAO`() = runTest {
        val ticket = Ticket("t1", "e1", "Event", "ref", "c1", "qr", 0L)

        repository.saveTicket(ticket)

        coVerify { ticketDao.insert(any()) }
    }

    @Test
    fun `getTicketByReference should return mapped ticket or null`() = runTest {
        val reference = "ref123"
        val entity = TicketEntity("t1", "e1", "Event", reference, "c1", "qr", 0L)

        coEvery { ticketDao.getByPurchaseReference(reference) } returns entity
        assertEquals("t1", repository.getTicketByReference(reference)?.ticketId)

        coEvery { ticketDao.getByPurchaseReference("none") } returns null
        assertNull(repository.getTicketByReference("none"))
    }
}
