package com.cielotickets.app.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Interface base para representar o estado da UI.
 */
interface UiState

/**
 * Interface base para representar as intenções do usuário (ações).
 */
interface UiIntent

/**
 * Interface base para efeitos colaterais únicos (navegação, toasts, etc).
 */
interface UiEffect

/**
 * ViewModel base que implementa o padrão MVI.
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEffect> : ViewModel() {

    abstract fun createInitialState(): S

    private val _uiState: MutableStateFlow<S> by lazy { MutableStateFlow(createInitialState()) }
    val uiState: StateFlow<S> by lazy { _uiState.asStateFlow() }

    private val _effect: Channel<E> = Channel()
    val effect = _effect.receiveAsFlow()

    private val _intent: MutableSharedFlow<I> = MutableSharedFlow()

    init {
        viewModelScope.launch {
            _intent.collect {
                handleIntent(it)
            }
        }
    }

    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    abstract fun handleIntent(intent: I)

    protected fun setState(reduce: S.() -> S) {
        _uiState.value = _uiState.value.reduce()
    }

    protected fun setEffect(builder: () -> E) {
        val effectValue = builder()
        viewModelScope.launch { _effect.send(effectValue) }
    }
}
