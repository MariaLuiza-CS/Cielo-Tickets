package com.cielotickets.app.presentation.receipt

import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.presentation.base.UiEffect
import com.cielotickets.app.presentation.base.UiIntent
import com.cielotickets.app.presentation.base.UiState

class ReceiptContract {

    data class State(
        val ticket: Ticket? = null,
        val event: Event? = null,
        val isLoading: Boolean = false
    ) : UiState

    sealed interface Intent : UiIntent {
        data class LoadReceipt(val ticketId: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}
