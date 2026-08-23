package com.cielotickets.app.presentation.events

import androidx.lifecycle.viewModelScope
import com.cielotickets.app.domain.usecase.GetEventsUseCase
import com.cielotickets.app.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(private val getEventsUseCase: GetEventsUseCase) :
    BaseViewModel<EventListContract.State, EventListContract.Intent, EventListContract.Effect>() {

    override fun createInitialState(): EventListContract.State = EventListContract.State()

    init {
        sendIntent(EventListContract.Intent.LoadEvents)
    }

    override fun handleIntent(intent: EventListContract.Intent) {
        when (intent) {
            is EventListContract.Intent.LoadEvents -> loadEvents()
            is EventListContract.Intent.RefreshEvents -> loadEvents()
            is EventListContract.Intent.EventClicked -> {
                setEffect { EventListContract.Effect.NavigateToDetail(intent.id) }
            }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            getEventsUseCase().fold(
                onSuccess = { events ->
                    setState { copy(isLoading = false, events = events) }
                },
                onFailure = { error ->
                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erro desconhecido ao carregar eventos",
                        )
                    }
                    setEffect {
                        EventListContract.Effect.ShowError(
                            error.message ?: "Não foi possível carregar os eventos",
                        )
                    }
                },
            )
        }
    }
}
