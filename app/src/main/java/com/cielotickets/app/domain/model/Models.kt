package com.cielotickets.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Event(val id: String, val name: String, val date: String, val location: String, val price: Double, val imageUrl: String, val availableTickets: Int)

@Serializable
data class Ticket(
    val ticketId: String,
    val eventId: String,
    val eventName: String,
    val purchaseReference: String,
    val cieloOrderId: String?,
    val qrPayload: String,
    val createdAt: Long,
)

sealed interface PaymentState {
    data object Idle : PaymentState
    data object Processing : PaymentState
    data class Approved(val orderId: String, val reference: String) : PaymentState
    data class Denied(val reason: String) : PaymentState
    data object Cancelled : PaymentState
    data class Error(val code: Int, val reason: String) : PaymentState
}
