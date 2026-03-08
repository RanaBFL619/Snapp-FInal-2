package com.snapp.presentation.util

/**
 * Maps backend/technical error messages to user-friendly strings for login and auth flows.
 * Use for toast/snackbar so users never see raw serialization or server messages.
 */
object AuthMessages {

    const val LOGIN_FAILED_GENERIC = "Invalid email or password. Please try again."
    const val LOGIN_SUCCESS = "Logged in successfully"
    const val NETWORK_ERROR = "No internet connection. Please check and try again."
    const val SERVER_ERROR = "Something went wrong. Please try again later."

    /**
     * Converts an exception/backend message to a short, user-friendly login error message.
     * Any technical-looking text is mapped to a generic message.
     */
    fun toUserFriendlyLoginError(throwableMessage: String?): String {
        if (throwableMessage.isNullOrBlank()) return LOGIN_FAILED_GENERIC
        val msg = throwableMessage.lowercase()
        return when {
            isNetworkRelated(msg) -> NETWORK_ERROR
            isServerError(msg) -> SERVER_ERROR
            isAuthFailure(msg) -> LOGIN_FAILED_GENERIC
            looksTechnical(msg) -> LOGIN_FAILED_GENERIC
            else -> LOGIN_FAILED_GENERIC
        }
    }

    /**
     * Use from ViewModel when you have the Throwable (checks message + cause).
     */
    fun toUserFriendlyLoginError(throwable: Throwable?): String {
        if (throwable == null) return LOGIN_FAILED_GENERIC
        val combined = buildString {
            throwable.message?.let { append(it) }
            throwable.cause?.message?.let { append(" ").append(it) }
        }.trim()
        return toUserFriendlyLoginError(combined.ifBlank { null })
    }

    private fun isNetworkRelated(msg: String): Boolean =
        msg.contains("unable to resolve host") || msg.contains("failed to connect") ||
        msg.contains("network") || msg.contains("timeout") || msg.contains("timed out") ||
        msg.contains("connection") || msg.contains("no route to host")

    private fun isServerError(msg: String): Boolean =
        msg.contains("500") || msg.contains("502") || msg.contains("503") ||
        msg.contains("internal server") || msg.contains("bad gateway")

    private fun isAuthFailure(msg: String): Boolean =
        msg.contains("401") || msg.contains("unauthorized") || msg.contains("invalid credentials") ||
        msg.contains("wrong password") || msg.contains("invalid login")

    private fun looksTechnical(msg: String): Boolean =
        msg.contains("serial") || msg.contains("serialization") || msg.contains("serial name") ||
        msg.contains("required for type") || msg.contains("were missing at path") ||
        msg.contains("missing at path") || msg.contains("path:") ||
        msg.contains("com.snapp") || msg.contains("kotlinx") || msg.contains("serializer") ||
        msg.contains("decode") || msg.contains("expected") && msg.contains("but") ||
        msg.contains("token") && msg.contains("required") ||
        msg.contains("username") && msg.contains("required") ||
        msg.contains("fields") && msg.contains("required")
}
