package com.snapp.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapp.data.model.auth.UserSession
import com.snapp.presentation.state.AuthUiState
import com.snapp.presentation.viewmodel.AuthSharedViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Platform ViewModel that exposes the shared auth store to the UI.
 * Auth state is the single source of truth (like Redux); when it changes, UI re-composes.
 * Initial state is read from persisted store so we don't flash Login when user has a session.
 */
class AuthViewModel(
    private val shared: AuthSharedViewModel
) : ViewModel() {

    /** Auth state from central store. Initial value from persisted session so first frame is correct. */
    val authState: StateFlow<AuthUiState> =
        shared.authState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = shared.getStoredSession()?.let { AuthUiState.Success(it) } ?: AuthUiState.Idle
        )

    val userSession: StateFlow<UserSession?> =
        shared.userSession.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    fun isLoggedIn(): Boolean = shared.isLoggedIn()
    fun login(username: String, password: String) = shared.login(username, password)
    fun logout() = shared.logout()
    fun clearError() = shared.clearError()
}
