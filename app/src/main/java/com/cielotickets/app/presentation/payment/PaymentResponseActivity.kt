package com.cielotickets.app.presentation.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.cielotickets.app.MainActivity
import com.cielotickets.app.data.payment.bus.PaymentCallbackBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PaymentResponseActivity : ComponentActivity() {

    @Inject
    lateinit var callbackBus: PaymentCallbackBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.data?.toString()?.let { uri ->
            lifecycleScope.launch {
                callbackBus.emit(uri)
            }
        }

        // Traz a MainActivity de volta para o topo da pilha
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(mainIntent)
        finish()
    }
}
