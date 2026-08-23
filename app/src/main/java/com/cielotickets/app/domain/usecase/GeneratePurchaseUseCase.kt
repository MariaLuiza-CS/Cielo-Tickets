package com.cielotickets.app.domain.usecase

import com.cielotickets.app.domain.repository.PaymentRepository
import java.util.UUID
import javax.inject.Inject

class GeneratePurchaseUseCase @Inject constructor(private val paymentRepository: PaymentRepository) {
    suspend operator fun invoke(eventId: String, quantity: Int, totalPriceCents: Int, existingIdempotencyKey: String? = null): String {
        if (existingIdempotencyKey != null) return existingIdempotencyKey

        val existingKey = paymentRepository.getExistingPendingKey(eventId, quantity)
        if (existingKey != null) return existingKey

        val key = UUID.randomUUID().toString()

        paymentRepository.savePendingPurchase(
            idempotencyKey = key,
            eventId = eventId,
            quantity = quantity,
            totalPriceCents = totalPriceCents,
        )

        return key
    }
}
