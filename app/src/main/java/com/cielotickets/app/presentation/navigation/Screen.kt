package com.cielotickets.app.presentation.navigation

sealed class Screen(val route: String) {
    data object EventList : Screen("event_list")
    data object MyTickets : Screen("my_tickets")
    data object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    data object Payment : Screen("payment/{eventId}/{quantity}/{totalPrice}") {
        fun createRoute(eventId: String, quantity: Int, totalPrice: Double) =
            "payment/$eventId/$quantity/$totalPrice"
    }
    data object Receipt : Screen("receipt/{ticketId}") {
        fun createRoute(ticketId: String) = "receipt/$ticketId"
    }
}
