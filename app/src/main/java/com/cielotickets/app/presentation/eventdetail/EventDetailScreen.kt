package com.cielotickets.app.presentation.eventdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cielotickets.app.presentation.util.toBrazilianCurrency
import com.cielotickets.app.presentation.util.toFormattedEventDate
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(viewModel: EventDetailViewModel, onNavigateBack: () -> Unit, onNavigateToPayment: (String, Int, Double) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EventDetailContract.Effect.NavigateToPayment -> {
                    onNavigateToPayment(effect.eventId, effect.quantity, effect.totalPrice)
                }

                is EventDetailContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null && state.event == null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                state.event?.let { event ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        AsyncImage(
                            model = event.imageUrl,
                            contentDescription = "Imagem do evento ${event.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop,
                        )

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📅 ${event.date.toFormattedEventDate()}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "📍 ${event.location}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider()

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Quantidade",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = { viewModel.sendIntent(EventDetailContract.Intent.DecreaseQuantity) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Diminuir quantidade de ingressos"
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = null,
                                    )
                                }
                                Text(
                                    text = state.quantity.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                                IconButton(
                                    onClick = { viewModel.sendIntent(EventDetailContract.Intent.IncreaseQuantity) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Aumentar quantidade de ingressos"
                                    },
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Disponíveis: ${event.availableTickets}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Total:",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                val totalPriceFormatted = state.totalPrice.toBrazilianCurrency()
                                Text(
                                    text = totalPriceFormatted,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = { viewModel.sendIntent(EventDetailContract.Intent.ProceedToPayment) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isLoading && state.event != null,
                            ) {
                                Text("Continuar para pagamento", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
