package com.cielotickets.app.presentation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Int.toBrazilianCurrency(): String {
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    return String.format(locale, "R$ %.2f", this / 100.0)
}
fun Double.toBrazilianCurrency(): String {
    return this.toInt().toBrazilianCurrency()
}

fun Long.toBrazilianDateTime(): String {
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
    return dateFormat.format(Date(this))
}
