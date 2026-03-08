package com.snapp.data.store

import com.russhwolf.settings.Settings
import com.snapp.data.model.auth.UserSession

/**
 * Persists auth session (token, username, name, roles, defaultPage) using [Settings]
 * (SharedPreferences on Android, UserDefaults on iOS). Session survives app restart so the user
 * stays logged in until they explicitly log out or the token is invalidated.
 */
class TokenStorage(private val settings: Settings) {

    fun getToken(): String? = settings.getStringOrNull(KEY_TOKEN)?.takeIf { it.isNotBlank() }

    fun saveSession(session: UserSession) {
        settings.putString(KEY_TOKEN, session.token)
        if (session.name != null) settings.putString(KEY_NAME, session.name)
        else settings.remove(KEY_NAME)
        settings.putString(KEY_USERNAME, session.username)
        settings.putString(KEY_ROLES, session.roles.joinToString(","))
        if (session.defaultPage != null) settings.putString(KEY_DEFAULT_PAGE, session.defaultPage)
        else settings.remove(KEY_DEFAULT_PAGE)
    }

    fun getSession(): UserSession? {
        val token = getToken() ?: return null
        val username = settings.getStringOrNull(KEY_USERNAME) ?: return null
        return UserSession(
            token = token,
            name = settings.getStringOrNull(KEY_NAME),
            username = username,
            roles = settings.getStringOrNull(KEY_ROLES)
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
            defaultPage = settings.getStringOrNull(KEY_DEFAULT_PAGE)
        )
    }

    fun clearSession() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_NAME)
        settings.remove(KEY_USERNAME)
        settings.remove(KEY_ROLES)
        settings.remove(KEY_DEFAULT_PAGE)
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_NAME = "auth_name"
        private const val KEY_USERNAME = "auth_username"
        private const val KEY_ROLES = "auth_roles"
        private const val KEY_DEFAULT_PAGE = "auth_default_page"
    }
}
