package com.cielotickets.app.presentation.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cielotickets.app.domain.repository.TicketRepository
import com.cielotickets.app.domain.usecase.GetEventByIdUseCase
import com.cielotickets.app.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ReceiptContract.State, ReceiptContract.Intent, ReceiptContract.Effect>() {

    private val ticketId: String? = savedStateHandle["ticketId"]

    override fun createInitialState(): ReceiptContract.State = ReceiptContract.State()

    init {
        ticketId?.let {
            sendIntent(ReceiptContract.Intent.LoadReceipt(it))
        }
    }

    override fun handleIntent(intent: ReceiptContract.Intent) {
        when (intent) {
            is ReceiptContract.Intent.LoadReceipt -> loadReceipt(intent.ticketId)
        }
    }

    private fun loadReceipt(id: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            ticketRepository.getTicketById(id).collectLatest { ticket ->
                if (ticket != null) {
                    val eventResult = getEventByIdUseCase(ticket.eventId)
                    eventResult.onSuccess { event ->
                        setState { copy(ticket = ticket, event = event, isLoading = false) }
                    }.onFailure {
                        setState { copy(ticket = ticket, isLoading = false) }
                        // Mesmo sem o evento, mostramos o ticket se ele existir
                    }
                } else {
                    setState { copy(isLoading = false) }
                    setEffect { ReceiptContract.Effect.ShowError("Comprovante não encontrado") }
                }
            }
        }
    }
}
