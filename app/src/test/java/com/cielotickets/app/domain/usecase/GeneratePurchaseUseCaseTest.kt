package com.cielotickets.app.domain.usecase

import com.cielotickets.app.domain.repository.PaymentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class GeneratePurchaseUseCaseTest {

    private val repository = mockk<PaymentRepository>(relaxed = true)
    private val useCase = GeneratePurchaseUseCase(repository)

    @Test
    fun `should generate and save new key when no pending purchase exists`() = runTest {
        // Arrange
        val eventId = "event1"
        val quantity = 2
        val totalPrice = 1000
        coEvery { repository.getExistingPendingKey(eventId, quantity) } returns null

        // Act
        val result = useCase(eventId, quantity, totalPrice)

        // Assert
        coVerify { repository.savePendingPurchase(result, eventId, quantity, totalPrice) }
        // Verify it's a valid UUID
        UUID.fromString(result)
    }

    @Test
    fun `should reuse existing key when pending purchase exists`() = runTest {
        // Arrange
        val eventId = "event1"
        val quantity = 2
        val totalPrice = 1000
        val existingKey = "existing-uuid"
        coEvery { repository.getExistingPendingKey(eventId, quantity) } returns existingKey

        // Act
        val result = useCase(eventId, quantity, totalPrice)

        // Assert
        assertEquals(existingKey, result)
        coVerify(exactly = 0) { repository.savePendingPurchase(any(), any(), any(), any()) }
    }
}
