package com.snapp.presentation.state

import com.snapp.data.model.auth.UserSession

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val session: UserSession) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
