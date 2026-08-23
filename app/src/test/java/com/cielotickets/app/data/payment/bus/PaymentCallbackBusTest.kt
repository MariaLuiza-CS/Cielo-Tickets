package com.cielotickets.app.data.payment.bus

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentCallbackBusTest {

    @Test
    fun `emit should be received by collectors`() = runTest {
        val bus = PaymentCallbackBus()
        val expectedData = "callback-data"

        bus.events.test {
            bus.emit(expectedData)
            assertEquals(expectedData, awaitItem())
        }
    }

    @Test
    fun `multiple collectors should receive the same emission`() = runTest {
        val bus = PaymentCallbackBus()
        val expectedData = "shared-data"

        val results = mutableListOf<String>()

        val job1 = launch {
            bus.events.collect { results.add(it) }
        }
        val job2 = launch {
            bus.events.collect { results.add(it) }
        }

        // Pequeno delay para garantir que os collectors estão registrados
        testScheduler.advanceUntilIdle()

        bus.emit(expectedData)

        // Delay para processar a emissão
        testScheduler.advanceUntilIdle()

        assertEquals(2, results.size)
        assertEquals(expectedData, results[0])
        assertEquals(expectedData, results[1])

        job1.cancel()
        job2.cancel()
    }
}
