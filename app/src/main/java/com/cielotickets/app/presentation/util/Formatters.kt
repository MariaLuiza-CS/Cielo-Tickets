package com.cielotickets.app.presentation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Int.toBrazilianCurrency(): String {
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    return String.format(locale, "R$ %.2f", this / 100.0)
}

fun Double.toBrazilianCurrency(): String = this.toInt().toBrazilianCurrency()

fun Long.toBrazilianDateTime(): String {
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
    dateFormat.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
    return dateFormat.format(Date(this))
}

fun String.toFormattedEventDate(): String {
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale)

    val date = try {
        inputFormat.parse(this)
    } catch (e: java.text.ParseException) {
        null
    } ?: return this

    val outputFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, 'às' HH'h'", locale)
    return outputFormat.format(date)
}
