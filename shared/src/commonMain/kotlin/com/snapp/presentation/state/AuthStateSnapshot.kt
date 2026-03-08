package com.snapp.presentation.state

import com.snapp.data.model.auth.UserSession

/**
 * Swift-friendly representation of auth state. Use this from iOS instead of
 * casting [AuthUiState] sealed subclasses, which may not export predictably to Swift.
 */
data class AuthStateSnapshot(
    val kind: String,
    val session: UserSession? = null,
    val errorMessage: String? = null
) {
    companion object {
        const val KIND_IDLE = "idle"
        const val KIND_LOADING = "loading"
        const val KIND_SUCCESS = "success"
        const val KIND_ERROR = "error"
    }
}
