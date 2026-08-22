package com.cielotickets.app.data.payment

object CieloDeepLinkConstants {
    const val PAYMENT_URI_SCHEME = "lio"
    const val PAYMENT_URI_HOST = "payment"
    const val CALLBACK_SCHEME = "order"
    const val CALLBACK_HOST = "response"
    const val CALLBACK_URI = "$CALLBACK_SCHEME://$CALLBACK_HOST"
}
