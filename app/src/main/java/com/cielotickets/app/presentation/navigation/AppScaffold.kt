package com.cielotickets.app.presentation.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Eventos") },
                    selected = currentRoute == Screen.EventList.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.EventList.route) {
                            popUpTo(Screen.EventList.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Event, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                )
                NavigationDrawerItem(
                    label = { Text("Meus Ingressos") },
                    selected = currentRoute == Screen.MyTickets.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.MyTickets.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                )
            }
        },
    ) {
        NavGraph(
            navController = navController,
            onOpenDrawer = {
                scope.launch { drawerState.open() }
            },
        )
    }
}
