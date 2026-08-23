package com.cielotickets.app.presentation.mytickets

import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.presentation.base.UiEffect
import com.cielotickets.app.presentation.base.UiIntent
import com.cielotickets.app.presentation.base.UiState

class MyTicketsContract {

    data class State(val tickets: List<Ticket> = emptyList(), val isLoading: Boolean = false, val isEmpty: Boolean = false) : UiState

    sealed interface Intent : UiIntent {
        data object LoadTickets : Intent
        data class TicketClicked(val ticketId: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToReceipt(val ticketId: String) : Effect
    }
}
