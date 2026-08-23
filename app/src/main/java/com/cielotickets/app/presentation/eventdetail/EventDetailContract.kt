package com.cielotickets.app.presentation.eventdetail

import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.presentation.base.UiEffect
import com.cielotickets.app.presentation.base.UiIntent
import com.cielotickets.app.presentation.base.UiState

class EventDetailContract {

    data class State(val event: Event? = null, val quantity: Int = 1, val isLoading: Boolean = false, val errorMessage: String? = null) : UiState {
        val totalPrice: Double
            get() = (event?.price ?: 0.0) * quantity
    }

    sealed interface Intent : UiIntent {
        data class LoadEvent(val eventId: String) : Intent
        data object IncreaseQuantity : Intent
        data object DecreaseQuantity : Intent
        data object ProceedToPayment : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToPayment(val eventId: String, val quantity: Int, val totalPrice: Double) : Effect
        data class ShowError(val message: String) : Effect
    }
}
