package com.cielotickets.app.presentation.receipt

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel,
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ReceiptContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Comprovante") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                state.ticket?.let { ticket ->
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Compra Aprovada!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            state.event?.let { event ->
                                Text(
                                    text = event.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "📅 ${event.date}")
                                Text(text = "📍 ${event.location}")
                            } ?: run {
                                Text(text = "Evento ID: ${ticket.eventId}")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    QrCodeImage(payload = ticket.qrPayload)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Apresente este QR Code na entrada do evento",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider()
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "ID do Ticket: ${ticket.ticketId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Transação Cielo: ${ticket.cieloOrderId ?: "N/A"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Button(
                        onClick = onNavigateToHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Voltar para Lista de Eventos")
                    }
                }
            }
        }
    }
}

@Composable
fun QrCodeImage(payload: String) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(payload) {
        withContext(Dispatchers.Default) {
            bitmap = generateQrCode(payload, 500)
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "QR Code do Ingresso",
            modifier = Modifier.size(200.dp)
        )
    } ?: run {
        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }
    }
}

private fun generateQrCode(text: String, size: Int): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(
        text,
        BarcodeFormat.QR_CODE,
        size,
        size
    )
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}
