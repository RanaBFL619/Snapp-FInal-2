package com.snapp.data.store

/**
 * Notifies listeners when a 401 Unauthorized is received from any API call (except auth endpoints).
 * Used to clear session and show login, matching web apiClient behavior.
 * Listeners are typically registered at app init (AuthViewModel, NetworkModule).
 */
class UnauthorizedNotifier {

    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    /** Called when 401 is received. Invokes all listeners (clear in-memory token, set auth state to idle). */
    fun notifyUnauthorized() {
        listeners.toList().forEach { it.invoke() }
    }
}
