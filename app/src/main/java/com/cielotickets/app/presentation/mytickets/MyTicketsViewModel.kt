package com.cielotickets.app.presentation.mytickets

import androidx.lifecycle.viewModelScope
import com.cielotickets.app.domain.usecase.GetMyTicketsUseCase
import com.cielotickets.app.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyTicketsViewModel @Inject constructor(private val getMyTicketsUseCase: GetMyTicketsUseCase) :
    BaseViewModel<MyTicketsContract.State, MyTicketsContract.Intent, MyTicketsContract.Effect>() {

    override fun createInitialState(): MyTicketsContract.State = MyTicketsContract.State()

    init {
        sendIntent(MyTicketsContract.Intent.LoadTickets)
    }

    override fun handleIntent(intent: MyTicketsContract.Intent) {
        when (intent) {
            is MyTicketsContract.Intent.LoadTickets -> loadTickets()
            is MyTicketsContract.Intent.TicketClicked -> {
                setEffect { MyTicketsContract.Effect.NavigateToReceipt(intent.ticketId) }
            }
        }
    }

    private fun loadTickets() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            getMyTicketsUseCase().collectLatest { tickets ->
                setState {
                    copy(
                        tickets = tickets,
                        isLoading = false,
                        isEmpty = tickets.isEmpty(),
                    )
                }
            }
        }
    }
}
