package com.cielotickets.app.data.payment.util

import android.util.Base64

object Base64Utils {
    fun encode(text: String): String {
        return Base64.encodeToString(text.toByteArray(), Base64.NO_WRAP)
    }

    fun decode(base64: String): String {
        return String(Base64.decode(base64, Base64.NO_WRAP))
    }
}
