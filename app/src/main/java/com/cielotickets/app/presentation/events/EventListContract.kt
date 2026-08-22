package com.cielotickets.app.presentation.events

import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.presentation.base.UiEffect
import com.cielotickets.app.presentation.base.UiIntent
import com.cielotickets.app.presentation.base.UiState

/**
 * Contrato MVI para a tela de listagem de eventos.
 */
class EventListContract {

    data class State(
        val events: List<Event> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadEvents : Intent
        data object RefreshEvents : Intent
        data class EventClicked(val id: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val eventId: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
