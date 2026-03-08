package com.snapp.presentation.state

/** UI state for record detail. See ARCHITECTURE presentation/state/. */
sealed class RecordUiState {
    data object Loading : RecordUiState()
    data object Success : RecordUiState()
    data class Error(val message: String) : RecordUiState()
}
