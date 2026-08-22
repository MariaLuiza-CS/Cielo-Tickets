package com.cielotickets.app.data.repository

import android.net.Uri
import com.cielotickets.app.BuildConfig
import com.cielotickets.app.data.local.dao.PendingPurchaseDao
import com.cielotickets.app.data.local.entity.PendingPurchaseEntity
import com.cielotickets.app.data.payment.CieloDeepLinkConstants
import com.cielotickets.app.data.payment.model.*
import com.cielotickets.app.data.payment.util.Base64Utils
import com.cielotickets.app.domain.model.PaymentState
import com.cielotickets.app.domain.repository.PaymentRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class PaymentRepositoryImpl @Inject constructor(
    private val pendingPurchaseDao: PendingPurchaseDao
) : PaymentRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    override fun buildPaymentUri(
        eventId: String,
        quantity: Int,
        unitPriceCents: Int,
        idempotencyKey: String
    ): String {
        val requestDto = PaymentRequestDto(
            accessToken = BuildConfig.CIELO_ACCESS_TOKEN,
            clientID = BuildConfig.CIELO_CLIENT_ID,
            reference = idempotencyKey,
            items = listOf(
                PaymentItemDto(
                    name = "Ingresso CieloTickets",
                    quantity = quantity,
                    sku = eventId,
                    unitPrice = unitPriceCents
                )
            ),
            value = unitPriceCents * quantity
        )

        val jsonString = json.encodeToString(requestDto)
        val base64Request = Base64Utils.encode(jsonString)

        return "${CieloDeepLinkConstants.PAYMENT_URI_SCHEME}://${CieloDeepLinkConstants.PAYMENT_URI_HOST}?request=$base64Request&urlCallback=${CieloDeepLinkConstants.CALLBACK_URI}"
    }

    override fun parsePaymentCallback(callbackData: String): PaymentState {
        return try {
            val uri = Uri.parse(callbackData)
            val base64Response = uri.getQueryParameter("response")
                ?: return PaymentState.Error(0, "Resposta vazia")
            val decodedJson = Base64Utils.decode(base64Response)
            val jsonElement = Json.parseToJsonElement(decodedJson).jsonObject

            if (jsonElement.containsKey("code")) {
                val errorResponse = json.decodeFromString<PaymentErrorDto>(decodedJson)
                when (errorResponse.code) {
                    1 -> PaymentState.Cancelled
                    else -> PaymentState.Error(errorResponse.code, errorResponse.reason)
                }
            } else {
                val successResponse = json.decodeFromString<PaymentResponseDto>(decodedJson)
                val statusCode = successResponse.payments
                    .firstOrNull()
                    ?.paymentFields
                    ?.get("statusCode")

                if (statusCode == "0" || statusCode == "1") {
                    PaymentState.Approved(successResponse.id, successResponse.reference)
                } else {
                    PaymentState.Denied("Pagamento não aprovado pela operadora")
                }
            }
        } catch (e: Exception) {
            PaymentState.Error(-1, e.message ?: "Erro ao processar retorno")
        }
    }

    override suspend fun savePendingPurchase(
        idempotencyKey: String,
        eventId: String,
        quantity: Int,
        totalPriceCents: Int
    ) {
        pendingPurchaseDao.insert(
            PendingPurchaseEntity(
                idempotencyKey = idempotencyKey,
                eventId = eventId,
                quantity = quantity,
                totalPriceCents = totalPriceCents,
                status = "PENDING"
            )
        )
    }

    override suspend fun getPendingPurchase(idempotencyKey: String): String? {
        return pendingPurchaseDao.getByKey(idempotencyKey)?.idempotencyKey
    }

    override suspend fun getExistingPendingKey(eventId: String, quantity: Int): String? {
        return pendingPurchaseDao.findPendingByEventAndQuantity(eventId, quantity)?.idempotencyKey
    }

    override suspend fun updatePurchaseStatus(idempotencyKey: String, status: String) {
        pendingPurchaseDao.updateStatus(idempotencyKey, status)
    }
}
