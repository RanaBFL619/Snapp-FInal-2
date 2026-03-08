package com.snapp.presentation.state

/** UI state for widget data. See ARCHITECTURE presentation/state/. */
sealed class WidgetUiState {
    data object Loading : WidgetUiState()
    data object Success : WidgetUiState()
    data class Error(val message: String) : WidgetUiState()
}
