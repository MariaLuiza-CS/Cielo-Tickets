package com.cielotickets.app.presentation.payment

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cielotickets.app.data.payment.bus.PaymentCallbackBus
import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.model.PaymentState
import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.domain.repository.PaymentRepository
import com.cielotickets.app.domain.repository.TicketRepository
import com.cielotickets.app.domain.usecase.CreateTicketUseCase
import com.cielotickets.app.domain.usecase.GeneratePurchaseUseCase
import com.cielotickets.app.domain.usecase.GetEventByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getEventByIdUseCase = mockk<GetEventByIdUseCase>()
    private val generatePurchaseUseCase = mockk<GeneratePurchaseUseCase>()
    private val paymentRepository = mockk<PaymentRepository>(relaxed = true)
    private val ticketRepository = mockk<TicketRepository>()
    private val createTicketUseCase = mockk<CreateTicketUseCase>()
    private val paymentCallbackBus = mockk<PaymentCallbackBus>()
    private val busFlow = MutableSharedFlow<String>()

    private val eventId = "event1"
    private val savedStateHandle =
        SavedStateHandle(mapOf("eventId" to eventId, "quantity" to 1, "totalPrice" to 1000f))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { paymentCallbackBus.events } returns busFlow
        coEvery { getEventByIdUseCase(any()) } returns Result.success(
            mockk<Event>(relaxed = true).apply {
                every { id } returns eventId
                every { price } returns 1000.0
            },
        )
        coEvery { generatePurchaseUseCase(any(), any(), any(), any()) } returns "current-key"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handleCallback should show error when reference does not match idempotencyKey`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        val callbackUri = "order://response?response=..."
        val approvedState = PaymentState.Approved(orderId = "order1", reference = "WRONG-KEY")

        every { paymentRepository.parsePaymentCallback(callbackUri) } returns approvedState

        // Act & Assert
        viewModel.effect.test {
            viewModel.sendIntent(PaymentContract.Intent.PaymentCallbackReceived(callbackUri))
            val effect = awaitItem()
            assertTrue(effect is PaymentContract.Effect.ShowError)
            coVerify(exactly = 0) { createTicketUseCase(any(), any(), any(), any()) }
        }
    }

    @Test
    fun `createTicket should reuse existing ticket when purchaseReference already exists`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        val currentKey = "current-key"
        val existingTicket = mockk<Ticket>(relaxed = true).apply {
            every { ticketId } returns "ticket-123"
        }

        coEvery { ticketRepository.getTicketByReference(currentKey) } returns existingTicket

        // Act & Assert
        viewModel.effect.test {
            // Chamamos diretamente o método privado createTicket via reflection ou simulando o sucesso do callback
            val callbackUri = "order://response?response=..."
            val approvedState =
                PaymentState.Approved(orderId = "order1", reference = currentKey)
            every { paymentRepository.parsePaymentCallback(callbackUri) } returns approvedState

            viewModel.sendIntent(PaymentContract.Intent.PaymentCallbackReceived(callbackUri))

            val effect = awaitItem()
            assertTrue(effect is PaymentContract.Effect.NavigateToReceipt)
            coVerify(exactly = 0) { createTicketUseCase(any(), any(), any(), any()) }
        }
    }

    private fun createViewModel() = PaymentViewModel(
        getEventByIdUseCase,
        generatePurchaseUseCase,
        paymentRepository,
        ticketRepository,
        createTicketUseCase,
        paymentCallbackBus,
        savedStateHandle,
    )
}
