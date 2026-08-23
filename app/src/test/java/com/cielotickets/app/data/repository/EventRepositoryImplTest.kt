package com.cielotickets.app.data.repository

import com.cielotickets.app.data.local.dao.EventDao
import com.cielotickets.app.data.local.entity.EventEntity
import com.cielotickets.app.data.remote.datasource.EventRemoteDataSource
import com.cielotickets.app.data.remote.model.EventDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventRepositoryImplTest {

    private val remoteDataSource = mockk<EventRemoteDataSource>()
    private val eventDao = mockk<EventDao>(relaxed = true)
    private val repository = EventRepositoryImpl(remoteDataSource, eventDao)

    @Test
    fun `getEvents should fetch from remote, save to local and return local data`() = runTest {
        // Arrange
        val remoteDto = EventDto(id = "1", name = "Remote")
        val localEntity = EventEntity(id = "1", name = "Local", date = "", location = "", price = 0.0, imageUrl = "", availableTickets = 0)

        coEvery { remoteDataSource.fetchEvents() } returns listOf(remoteDto)
        every { eventDao.getAllEvents() } returns flowOf(listOf(localEntity))

        // Act
        val result = repository.getEvents()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Local", result.getOrNull()?.first()?.name)
        coVerify { eventDao.clearAll() }
        coVerify { eventDao.insertEvents(any()) }
    }

    @Test
    fun `getEvents should return local cache when remote fails`() = runTest {
        // Arrange
        val localEntity = EventEntity(id = "1", name = "Cached", date = "", location = "", price = 0.0, imageUrl = "", availableTickets = 0)

        coEvery { remoteDataSource.fetchEvents() } throws Exception("Network error")
        every { eventDao.getAllEvents() } returns flowOf(listOf(localEntity))

        // Act
        val result = repository.getEvents()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Cached", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `getEvents should return failure when both remote and local fail`() = runTest {
        // Arrange
        coEvery { remoteDataSource.fetchEvents() } throws Exception("Network error")
        every { eventDao.getAllEvents() } returns flowOf(emptyList())

        // Act
        val result = repository.getEvents()

        // Assert
        assertTrue(result.isFailure)
    }
}
