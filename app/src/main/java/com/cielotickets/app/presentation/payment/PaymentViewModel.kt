package com.cielotickets.app.presentation.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cielotickets.app.BuildConfig
import com.cielotickets.app.data.payment.bus.PaymentCallbackBus
import com.cielotickets.app.domain.model.PaymentState
import com.cielotickets.app.domain.repository.PaymentRepository
import com.cielotickets.app.domain.repository.TicketRepository
import com.cielotickets.app.domain.usecase.CreateTicketUseCase
import com.cielotickets.app.domain.usecase.GeneratePurchaseUseCase
import com.cielotickets.app.domain.usecase.GetEventByIdUseCase
import com.cielotickets.app.presentation.base.BaseViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val generatePurchaseUseCase: GeneratePurchaseUseCase,
    private val paymentRepository: PaymentRepository,
    private val ticketRepository: TicketRepository,
    private val createTicketUseCase: CreateTicketUseCase,
    private val paymentCallbackBus: PaymentCallbackBus,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PaymentContract.State, PaymentContract.Intent, PaymentContract.Effect>() {

    private var idempotencyKey: String? = null

    private val eventId: String? = savedStateHandle["eventId"]
    private val quantity: Int = savedStateHandle["quantity"] ?: 0
    private val totalPriceCents: Float = savedStateHandle["totalPrice"] ?: 0f

    override fun createInitialState(): PaymentContract.State = PaymentContract.State()

    init {
        eventId?.let {
            sendIntent(PaymentContract.Intent.LoadPaymentInfo(it, quantity, totalPriceCents.toInt()))
        }

        viewModelScope.launch {
            paymentCallbackBus.events.collectLatest { rawUri ->
                sendIntent(PaymentContract.Intent.PaymentCallbackReceived(rawUri))
            }
        }
    }

    override fun handleIntent(intent: PaymentContract.Intent) {
        when (intent) {
            is PaymentContract.Intent.LoadPaymentInfo -> loadPaymentInfo(intent)
            is PaymentContract.Intent.StartPayment -> startPayment()
            is PaymentContract.Intent.PaymentCallbackReceived -> handleCallback(intent.rawUri)
        }
    }

    private fun loadPaymentInfo(intent: PaymentContract.Intent.LoadPaymentInfo) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            getEventByIdUseCase(intent.eventId).onSuccess { event ->
                idempotencyKey = generatePurchaseUseCase(
                    eventId = event.id,
                    quantity = intent.quantity,
                    totalPriceCents = intent.totalPriceCents,
                    existingIdempotencyKey = idempotencyKey,
                )

                setState {
                    copy(
                        event = event,
                        quantity = intent.quantity,
                        totalPriceCents = intent.totalPriceCents,
                        isLoading = false,
                    )
                }
            }.onFailure {
                setState { copy(isLoading = false) }
                setEffect { PaymentContract.Effect.ShowError("Erro ao carregar dados do evento") }
            }
        }
    }

    private fun startPayment() {
        val state = uiState.value
        val event = state.event ?: return
        val key = idempotencyKey ?: return

        // Validação de credenciais da Cielo
        if (BuildConfig.CIELO_CLIENT_ID.isBlank() || BuildConfig.CIELO_ACCESS_TOKEN.isBlank()) {
            setEffect {
                PaymentContract.Effect.ShowError(
                    "Credenciais da Cielo não configuradas. Adicione CIELO_CLIENT_ID e CIELO_ACCESS_TOKEN ao local.properties para testar o pagamento.",
                )
            }
            return
        }

        setState { copy(paymentState = PaymentState.Processing) }

        val uri = paymentRepository.buildPaymentUri(
            eventId = event.id,
            quantity = state.quantity,
            unitPriceCents = (event.price).toInt(),
            idempotencyKey = key,
        )

        setEffect { PaymentContract.Effect.LaunchPaymentIntent(uri) }
    }

    private fun handleCallback(rawUri: String) {
        try {
            val resultState = paymentRepository.parsePaymentCallback(rawUri)
            val currentKey = idempotencyKey ?: ""

            viewModelScope.launch {
                when (resultState) {
                    is PaymentState.Approved -> {
                        if (resultState.reference == currentKey) {
                            paymentRepository.updatePurchaseStatus(currentKey, "PAID")
                            setState { copy(paymentState = resultState) }
                            createTicket(resultState.orderId)
                        } else {
                            val error = PaymentState.Error(0, "Resposta de pagamento não corresponde a esta compra")
                            setState { copy(paymentState = error) }
                            setEffect { PaymentContract.Effect.ShowError(error.reason) }
                        }
                    }
                    is PaymentState.Denied -> {
                        paymentRepository.updatePurchaseStatus(currentKey, "DENIED")
                        setState { copy(paymentState = resultState) }
                    }
                    is PaymentState.Cancelled -> {
                        paymentRepository.updatePurchaseStatus(currentKey, "CANCELLED")
                        setState { copy(paymentState = resultState) }
                    }
                    is PaymentState.Error -> {
                        paymentRepository.updatePurchaseStatus(currentKey, "ERROR")
                        setState { copy(paymentState = resultState) }
                        setEffect { PaymentContract.Effect.ShowError("Erro no pagamento: ${resultState.reason}") }
                        FirebaseCrashlytics.getInstance().apply {
                            setCustomKey("cielo_error_code", resultState.code)
                            setCustomKey("idempotency_key", currentKey)
                            recordException(Exception("Erro de pagamento Cielo: ${resultState.reason}"))
                        }
                    }
                    else -> {
                        setState { copy(paymentState = resultState) }
                    }
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            setState { copy(paymentState = PaymentState.Error(-1, "Erro inesperado ao processar retorno")) }
        }
    }

    private fun createTicket(cieloOrderId: String) {
        viewModelScope.launch {
            val state = uiState.value
            val event = state.event ?: return@launch
            val key = idempotencyKey ?: return@launch

            // Verifica se já existe um ticket para esta referência (idempotência)
            val existingTicket = ticketRepository.getTicketByReference(key)
            if (existingTicket != null) {
                setState { copy(generatedTicket = existingTicket) }
                setEffect { PaymentContract.Effect.NavigateToReceipt(existingTicket.ticketId) }
                return@launch
            }

            val ticket = createTicketUseCase(
                eventId = event.id,
                eventName = event.name,
                purchaseReference = key,
                cieloOrderId = cieloOrderId,
            )

            setState { copy(generatedTicket = ticket) }
            setEffect { PaymentContract.Effect.NavigateToReceipt(ticket.ticketId) }
        }
    }
}
