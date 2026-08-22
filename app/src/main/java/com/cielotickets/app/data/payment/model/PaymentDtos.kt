package com.cielotickets.app.data.payment.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequestDto(
    val accessToken: String,
    val clientID: String,
    val reference: String,
    val items: List<PaymentItemDto>,
    val paymentCode: String? = null,
    val value: Int // Valor total em centavos
)

@Serializable
data class PaymentItemDto(
    val name: String,
    val quantity: Int,
    val sku: String,
    val unitOfMeasure: String = "EACH",
    val unitPrice: Int
)

@Serializable
data class PaymentResponseDto(
    val id: String,
    val status: String,
    val reference: String,
    val payments: List<PaymentEntryDto> = emptyList()
)

@Serializable
data class PaymentEntryDto(
    val id: String,
    val amount: Long? = null,
    val paymentFields: Map<String, String> = emptyMap()
)

@Serializable
data class PaymentErrorDto(
    val code: Int,
    val reason: String
)
