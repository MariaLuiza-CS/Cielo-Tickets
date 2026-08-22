package com.cielotickets.app.presentation.eventdetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.usecase.GetEventByIdUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getEventByIdUseCase = mockk<GetEventByIdUseCase>()
    private val eventId = "event1"
    private val savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getEventByIdUseCase(eventId) } returns Result.success(
            Event(
                id = eventId,
                name = "Test Event",
                date = "2024-01-01",
                location = "Test Location",
                price = 100.0,
                imageUrl = "",
                availableTickets = 2
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `increaseQuantity should not exceed availableTickets`() = runTest {
        // Arrange
        val viewModel = EventDetailViewModel(getEventByIdUseCase, savedStateHandle)

        // Act
        viewModel.sendIntent(EventDetailContract.Intent.IncreaseQuantity) // 1 -> 2
        viewModel.sendIntent(EventDetailContract.Intent.IncreaseQuantity) // 2 -> 2 (limit)

        // Assert
        viewModel.uiState.test {
            assertEquals(2, awaitItem().quantity)
        }
    }

    @Test
    fun `decreaseQuantity should not go below 1`() = runTest {
        // Arrange
        val viewModel = EventDetailViewModel(getEventByIdUseCase, savedStateHandle)

        // Act
        viewModel.sendIntent(EventDetailContract.Intent.DecreaseQuantity) // 1 -> 1 (limit)

        // Assert
        viewModel.uiState.test {
            assertEquals(1, awaitItem().quantity)
        }
    }
}
