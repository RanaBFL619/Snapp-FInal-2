package com.snapp.presentation.state

import com.snapp.data.model.layout.LayoutConfig

sealed class LayoutUiState {
    data object Loading : LayoutUiState()
    data class Success(val layout: LayoutConfig) : LayoutUiState()
    data class Error(val message: String) : LayoutUiState()
}
