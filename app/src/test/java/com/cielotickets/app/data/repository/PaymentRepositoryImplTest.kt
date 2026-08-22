package com.cielotickets.app.data.repository

import com.cielotickets.app.data.local.dao.PendingPurchaseDao
import com.cielotickets.app.data.payment.util.Base64Utils
import com.cielotickets.app.domain.model.PaymentState
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PaymentRepositoryImplTest {

    private val dao = mockk<PendingPurchaseDao>(relaxed = true)
    private val repository = PaymentRepositoryImpl(dao)

    @Test
    fun `parsePaymentCallback should return Approved when statusCode is 1`() {
        // Arrange
        // JSON: {"id":"order123","status":"APPROVED","reference":"ref123","payments":[{"id":"pay1","paymentFields":{"statusCode":"1"}}]}
        val json = """{"id":"order123","status":"APPROVED","reference":"ref123","payments":[{"id":"pay1","paymentFields":{"statusCode":"1"}}]}"""
        val base64 = Base64Utils.encode(json)
        val callback = "order://response?response=$base64"

        // Act
        val result = repository.parsePaymentCallback(callback)

        // Assert
        assertTrue(result is PaymentState.Approved)
        assertEquals("order123", (result as PaymentState.Approved).orderId)
        assertEquals("ref123", result.reference)
    }

    @Test
    fun `parsePaymentCallback should return Denied when statusCode is not 1`() {
        // Arrange
        // JSON: {"id":"order123","status":"DENIED","reference":"ref123","payments":[{"id":"pay1","paymentFields":{"statusCode":"2"}}]}
        val json = """{"id":"order123","status":"DENIED","reference":"ref123","payments":[{"id":"pay1","paymentFields":{"statusCode":"2"}}]}"""
        val base64 = Base64Utils.encode(json)
        val callback = "order://response?response=$base64"

        // Act
        val result = repository.parsePaymentCallback(callback)

        // Assert
        assertTrue(result is PaymentState.Denied)
    }

    @Test
    fun `parsePaymentCallback should return Cancelled when error code is 1`() {
        // Arrange
        // JSON: {"code":1,"reason":"User cancelled"}
        val json = """{"code":1,"reason":"User cancelled"}"""
        val base64 = Base64Utils.encode(json)
        val callback = "order://response?response=$base64"

        // Act
        val result = repository.parsePaymentCallback(callback)

        // Assert
        assertTrue(result is PaymentState.Cancelled)
    }

    @Test
    fun `parsePaymentCallback should return Error when error code is 2, 3 or 4`() {
        // Arrange
        val json = """{"code":2,"reason":"Network error"}"""
        val base64 = Base64Utils.encode(json)
        val callback = "order://response?response=$base64"

        // Act
        val result = repository.parsePaymentCallback(callback)

        // Assert
        assertTrue(result is PaymentState.Error)
        assertEquals(2, (result as PaymentState.Error).code)
    }

    @Test
    fun `parsePaymentCallback should return Error on malformed JSON`() {
        // Arrange
        val base64 = Base64Utils.encode("invalid-json")
        val callback = "order://response?response=$base64"

        // Act
        val result = repository.parsePaymentCallback(callback)

        // Assert
        assertTrue(result is PaymentState.Error)
    }
}
