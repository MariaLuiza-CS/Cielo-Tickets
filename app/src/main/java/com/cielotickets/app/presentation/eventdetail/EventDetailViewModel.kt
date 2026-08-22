package com.cielotickets.app.presentation.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cielotickets.app.domain.usecase.GetEventByIdUseCase
import com.cielotickets.app.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val getEventByIdUseCase: GetEventByIdUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<EventDetailContract.State, EventDetailContract.Intent, EventDetailContract.Effect>() {

    private val eventId: String? = savedStateHandle["eventId"]

    override fun createInitialState(): EventDetailContract.State {
        return EventDetailContract.State()
    }

    init {
        eventId?.let {
            sendIntent(EventDetailContract.Intent.LoadEvent(it))
        } ?: run {
            setState { copy(errorMessage = "ID do evento inválido") }
        }
    }

    override fun handleIntent(intent: EventDetailContract.Intent) {
        when (intent) {
            is EventDetailContract.Intent.LoadEvent -> loadEvent(intent.eventId)
            is EventDetailContract.Intent.IncreaseQuantity -> increaseQuantity()
            is EventDetailContract.Intent.DecreaseQuantity -> decreaseQuantity()
            is EventDetailContract.Intent.ProceedToPayment -> proceedToPayment()
        }
    }

    private fun loadEvent(id: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            getEventByIdUseCase(id).fold(
                onSuccess = { event ->
                    setState { copy(isLoading = false, event = event) }
                },
                onFailure = { error ->
                    setState { 
                        copy(
                            isLoading = false, 
                            errorMessage = error.message ?: "Erro ao carregar detalhes" 
                        ) 
                    }
                }
            )
        }
    }

    private fun increaseQuantity() {
        val currentEvent = uiState.value.event ?: return
        if (uiState.value.quantity < currentEvent.availableTickets) {
            setState { copy(quantity = quantity + 1) }
        } else {
            setEffect { EventDetailContract.Effect.ShowError("Quantidade máxima atingida") }
        }
    }

    private fun decreaseQuantity() {
        if (uiState.value.quantity > 1) {
            setState { copy(quantity = quantity - 1) }
        }
    }

    private fun proceedToPayment() {
        val state = uiState.value
        val event = state.event ?: return
        setEffect { 
            EventDetailContract.Effect.NavigateToPayment(
                eventId = event.id,
                quantity = state.quantity,
                totalPrice = state.totalPrice
            ) 
        }
    }
}
