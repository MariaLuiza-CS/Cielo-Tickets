package com.cielotickets.app.data.payment.bus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentCallbackBus @Inject constructor() {
    private val _events = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    suspend fun emit(callbackData: String) {
        _events.emit(callbackData)
    }
}
