package com.snapp.presentation.viewmodel

import com.snapp.domain.usecase.layout.GetLayoutUseCase
import com.snapp.presentation.state.LayoutStateSnapshot
import com.snapp.presentation.state.LayoutUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LayoutSharedViewModel(
    private val getLayoutUseCase: GetLayoutUseCase,
    private val scope: CoroutineScope
) {
    private val _layoutState = MutableStateFlow<LayoutUiState>(LayoutUiState.Loading)
    val layoutState: StateFlow<LayoutUiState> = _layoutState.asStateFlow()

    fun loadLayout() {
        scope.launch {
            _layoutState.value = LayoutUiState.Loading
            try {
                val layout = getLayoutUseCase()
                _layoutState.value = LayoutUiState.Success(layout)
            } catch (e: Exception) {
                _layoutState.value = LayoutUiState.Error(e.message ?: "Failed to load layout")
            }
        }
    }

    /** iOS StateFlow observation bridge — emits LayoutStateSnapshot (Swift-friendly). */
    fun collectLayoutStateSnapshot(onState: (LayoutStateSnapshot) -> Unit) {
        scope.launch {
            layoutState.collect { state ->
                onState(
                    when (state) {
                        is LayoutUiState.Loading -> LayoutStateSnapshot(LayoutStateSnapshot.KIND_LOADING)
                        is LayoutUiState.Success -> LayoutStateSnapshot(LayoutStateSnapshot.KIND_SUCCESS, layout = state.layout)
                        is LayoutUiState.Error -> LayoutStateSnapshot(LayoutStateSnapshot.KIND_ERROR, errorMessage = state.message)
                    }
                )
            }
        }
    }
}
