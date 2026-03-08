package com.snapp.presentation.viewmodel

import com.snapp.data.model.auth.UserSession
import com.snapp.data.store.UnauthorizedNotifier
import com.snapp.domain.repository.AuthRepository
import com.snapp.domain.validation.LoginValidation
import com.snapp.domain.validation.ValidationResult
import com.snapp.domain.usecase.auth.LoginUseCase
import com.snapp.domain.usecase.auth.LogoutUseCase
import com.snapp.presentation.state.AuthStateSnapshot
import com.snapp.presentation.state.AuthUiState
import com.snapp.presentation.util.AuthMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Central auth store (single source of truth). Persisted via [AuthRepository.getSession] / TokenStorage.
 * On init, restores session from disk so UI shows logged-in state without flashing login.
 * When auth state changes (login/logout/401), UI observes and re-renders (StateFlow).
 */
class AuthSharedViewModel(
    private val scope: CoroutineScope,
    private val authRepository: AuthRepository,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val unauthorizedNotifier: UnauthorizedNotifier
) {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val userSession: StateFlow<UserSession?> = authState
        .map { (it as? AuthUiState.Success)?.session }
        .stateIn(scope, SharingStarted.Eagerly, null)

    init {
        runCatching { authRepository.getSession() }.getOrNull()?.let { restoredSession ->
            _authState.value = AuthUiState.Success(restoredSession)
        }
        unauthorizedNotifier.addListener {
            scope.launch { _authState.update { AuthUiState.Idle } }
        }
    }

    fun isLoggedIn(): Boolean = _authState.value is AuthUiState.Success

    /** Returns the current stored session (from disk) if any. Use for synchronous restore on platform init (e.g. iOS) to avoid flashing Login. */
    fun getStoredSession(): UserSession? = authRepository.getSession()

    fun login(username: String, password: String) {
        val validationError = validate(username, password) ?: run {
            _authState.update { AuthUiState.Loading }
            scope.launch {
                loginUseCase(username, password)
                    .onSuccess { response ->
                        _authState.update {
                            AuthUiState.Success(
                                UserSession(
                                    token = response.token,
                                    name = response.name,
                                    username = response.username,
                                    roles = response.roles ?: emptyList(),
                                    defaultPage = response.defaultPage
                                )
                            )
                        }
                    }
                    .onFailure { e ->
                        _authState.update { AuthUiState.Error(AuthMessages.toUserFriendlyLoginError(e)) }
                    }
            }
            return
        }
        _authState.update { AuthUiState.Error(validationError) }
    }

    suspend fun performLogin(username: String, password: String): Boolean {
        val validationError = validate(username, password)
        if (validationError != null) {
            _authState.update { AuthUiState.Error(validationError) }
            return false
        }
        _authState.update { AuthUiState.Loading }
        return loginUseCase(username, password)
            .onSuccess { response ->
                _authState.update {
                    AuthUiState.Success(
                        UserSession(
                            token = response.token,
                            name = response.name,
                            username = response.username,
                            roles = response.roles ?: emptyList(),
                            defaultPage = response.defaultPage
                        )
                    )
                }
            }
            .onFailure { e ->
                _authState.update { AuthUiState.Error(AuthMessages.toUserFriendlyLoginError(e)) }
            }
            .isSuccess
    }

    fun performLoginWithCallback(username: String, password: String, callback: (Boolean) -> Unit) {
        scope.launch {
            callback(performLogin(username, password))
        }
    }

    fun logout() {
        scope.launch {
            logoutUseCase()
            _authState.update { AuthUiState.Idle }
        }
    }

    fun clearError() {
        if (_authState.value is AuthUiState.Error) _authState.update { AuthUiState.Idle }
    }

    /** iOS StateFlow observation bridge — collects state as AuthStateSnapshot (Swift-friendly, no sealed subclass types). */
    fun collectAuthStateSnapshot(onState: (AuthStateSnapshot) -> Unit) {
        scope.launch {
            authState.collect { state ->
                onState(
                    when (state) {
                        is AuthUiState.Success -> AuthStateSnapshot(AuthStateSnapshot.KIND_SUCCESS, session = state.session, errorMessage = null)
                        is AuthUiState.Loading -> AuthStateSnapshot(AuthStateSnapshot.KIND_LOADING, null, null)
                        is AuthUiState.Error -> AuthStateSnapshot(AuthStateSnapshot.KIND_ERROR, null, state.message)
                        else -> AuthStateSnapshot(AuthStateSnapshot.KIND_IDLE, null, null)
                    }
                )
            }
        }
    }

    private fun validate(username: String, password: String): String? {
        val (userResult, passResult) = LoginValidation.validateLogin(username, password)
        return (userResult as? ValidationResult.Invalid)?.message
            ?: (passResult as? ValidationResult.Invalid)?.message
    }
}
