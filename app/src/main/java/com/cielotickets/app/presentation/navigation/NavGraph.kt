package com.cielotickets.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cielotickets.app.presentation.eventdetail.EventDetailScreen
import com.cielotickets.app.presentation.eventdetail.EventDetailViewModel
import com.cielotickets.app.presentation.events.EventListScreen
import com.cielotickets.app.presentation.events.EventListViewModel
import com.cielotickets.app.presentation.mytickets.MyTicketsScreen
import com.cielotickets.app.presentation.mytickets.MyTicketsViewModel
import com.cielotickets.app.presentation.payment.PaymentScreen
import com.cielotickets.app.presentation.payment.PaymentViewModel
import com.cielotickets.app.presentation.receipt.ReceiptScreen
import com.cielotickets.app.presentation.receipt.ReceiptViewModel

@Composable
fun NavGraph(navController: NavHostController, onOpenDrawer: () -> Unit) {
    NavHost(
        navController = navController,
        startDestination = Screen.EventList.route,
    ) {
        composable(Screen.EventList.route) {
            val viewModel: EventListViewModel = hiltViewModel()
            EventListScreen(
                viewModel = viewModel,
                onMenuClick = onOpenDrawer,
                onNavigateToDetail = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
            )
        }

        composable(Screen.MyTickets.route) {
            val viewModel: MyTicketsViewModel = hiltViewModel()
            MyTicketsScreen(
                viewModel = viewModel,
                onMenuClick = onOpenDrawer,
                onNavigateToReceipt = { ticketId ->
                    navController.navigate(Screen.Receipt.createRoute(ticketId))
                },
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = androidx.navigation.NavType.StringType }),
        ) {
            val viewModel: EventDetailViewModel = hiltViewModel()
            EventDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPayment = { eventId, quantity, totalPrice ->
                    navController.navigate(Screen.Payment.createRoute(eventId, quantity, totalPrice))
                },
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("eventId") { type = androidx.navigation.NavType.StringType },
                navArgument("quantity") { type = androidx.navigation.NavType.IntType },
                navArgument("totalPrice") { type = androidx.navigation.NavType.FloatType },
            ),
        ) {
            val viewModel: PaymentViewModel = hiltViewModel()
            PaymentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReceipt = { ticketId ->
                    navController.navigate(Screen.Receipt.createRoute(ticketId))
                },
            )
        }

        composable(
            route = Screen.Receipt.route,
            arguments = listOf(navArgument("ticketId") { type = androidx.navigation.NavType.StringType }),
        ) {
            val viewModel: ReceiptViewModel = hiltViewModel()
            ReceiptScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.EventList.route) {
                        popUpTo(Screen.EventList.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
