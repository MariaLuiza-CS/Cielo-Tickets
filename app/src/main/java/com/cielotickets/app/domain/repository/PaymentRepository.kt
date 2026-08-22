package com.cielotickets.app.domain.repository

import com.cielotickets.app.domain.model.PaymentState

interface PaymentRepository {
    fun buildPaymentUri(
        eventId: String, 
        quantity: Int, 
        unitPriceCents: Int, 
        idempotencyKey: String
    ): String

    fun parsePaymentCallback(callbackData: String): PaymentState

    suspend fun savePendingPurchase(
        idempotencyKey: String,
        eventId: String,
        quantity: Int,
        totalPriceCents: Int
    )

    suspend fun getPendingPurchase(idempotencyKey: String): String?

    suspend fun getExistingPendingKey(eventId: String, quantity: Int): String?

    suspend fun updatePurchaseStatus(idempotencyKey: String, status: String)
}
