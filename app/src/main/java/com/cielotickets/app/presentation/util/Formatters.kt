package com.cielotickets.app.presentation.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Int.toBrazilianCurrency(): String {
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    return String.format(locale, "R$ %.2f", this / 100.0)
}

fun Double.toBrazilianCurrency(): String = this.toInt().toBrazilianCurrency()

fun Long.toBrazilianDateTime(): String {
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
    return dateFormat.format(Date(this))
}

/**
 * Formata uma data ISO 8601 para o padrão brasileiro de eventos.
 * Ex: "2026-10-15T22:00:00" -> "15 de outubro de 2026, às 22h"
 */
fun String.toFormattedEventDate(): String {
    return try {
        val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale)
        val date = inputFormat.parse(this) ?: return this

        val outputFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, 'às' HH'h'", locale)
        outputFormat.format(date)
    } catch (e: Exception) {
        this // Retorna o original em caso de erro
    }
}
