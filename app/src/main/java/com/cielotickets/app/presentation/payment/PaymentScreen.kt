package com.cielotickets.app.presentation.payment

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cielotickets.app.domain.model.PaymentState
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PaymentContract.Effect.LaunchPaymentIntent -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.uri)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        viewModel.sendIntent(PaymentContract.Intent.PaymentCallbackReceived("error://response?response=eyJjb2RlIjo0LCJyZWFzb24iOiJBcHAgQ2llbG8gU21hcnQgbsOjbyBlbmNvbnRyYWRvIn0="))
                    }
                }
                is PaymentContract.Effect.NavigateToReceipt -> {
                    onNavigateToReceipt(effect.ticketId)
                }
                is PaymentContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            state.event?.let { event ->
                Text(
                    text = "Resumo da Compra",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Evento:", fontWeight = FontWeight.Bold)
                            Text(text = event.name)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Quantidade:", fontWeight = FontWeight.Bold)
                            Text(text = "${state.quantity}x")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            val totalFormatted = String.format(Locale.getDefault(), "R$ %.2f", state.totalPriceCents / 100.0)
                            Text(
                                text = totalFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PaymentStatusView(state.paymentState)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.sendIntent(PaymentContract.Intent.StartPayment) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.paymentState !is PaymentState.Processing && !state.isLoading,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(16.dp)
            ) {
                if (state.paymentState is PaymentState.Processing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Pagar com Cielo Smart", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun PaymentStatusView(paymentState: PaymentState) {
    when (paymentState) {
        is PaymentState.Processing -> {
            Text(
                text = "Aguardando terminal Cielo...",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        is PaymentState.Denied -> {
            Text(
                text = "❌ ${paymentState.reason}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        is PaymentState.Error -> {
            Text(
                text = "⚠️ Erro: ${paymentState.reason}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        is PaymentState.Cancelled -> {
            Text(
                text = "Operação cancelada",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        else -> {}
    }
}
