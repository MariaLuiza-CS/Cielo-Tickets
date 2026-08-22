package com.cielotickets.app.presentation.events

import app.cash.turbine.test
import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.usecase.GetEventsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getEventsUseCase = mockk<GetEventsUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadEvents with success should update state correctly`() = runTest {
        // Arrange
        val events = listOf(mockk<Event>())
        coEvery { getEventsUseCase() } returns Result.success(events)

        // Act
        val viewModel = EventListViewModel(getEventsUseCase)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(events, state.events)
            assertFalse(state.isLoading)
            assertEquals(null, state.errorMessage)
        }
    }

    @Test
    fun `loadEvents with failure should update errorMessage`() = runTest {
        // Arrange
        val errorMsg = "Network error"
        coEvery { getEventsUseCase() } returns Result.failure(Exception(errorMsg))

        // Act
        val viewModel = EventListViewModel(getEventsUseCase)

        // Assert
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMsg, state.errorMessage)
            assertFalse(state.isLoading)
        }
    }
}
