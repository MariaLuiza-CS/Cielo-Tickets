package com.cielotickets.app.presentation.payment

import com.cielotickets.app.domain.model.Event
import com.cielotickets.app.domain.model.PaymentState
import com.cielotickets.app.domain.model.Ticket
import com.cielotickets.app.presentation.base.UiEffect
import com.cielotickets.app.presentation.base.UiIntent
import com.cielotickets.app.presentation.base.UiState

class PaymentContract {

    data class State(
        val event: Event? = null,
        val quantity: Int = 0,
        val totalPriceCents: Int = 0,
        val paymentState: PaymentState = PaymentState.Idle,
        val generatedTicket: Ticket? = null,
        val isLoading: Boolean = false,
    ) : UiState

    sealed interface Intent : UiIntent {
        data class LoadPaymentInfo(val eventId: String, val quantity: Int, val totalPriceCents: Int) : Intent
        data object StartPayment : Intent
        data class PaymentCallbackReceived(val rawUri: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class LaunchPaymentIntent(val uri: String) : Effect
        data class NavigateToReceipt(val ticketId: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
