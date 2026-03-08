package com.snapp.data.store

import com.russhwolf.settings.Settings

/**
 * General-purpose key-value store for non-auth data (e.g. preferences, feature flags, draft state).
 * Persists via the same Settings backend as [TokenStorage] (SharedPreferences / NSUserDefaults).
 * Injectable via Koin so any screen or ViewModel can read/write.
 */
class AppPreferenceStore(private val settings: Settings) {

    fun getString(key: String): String? =
        settings.getStringOrNull(prefix(key))?.takeIf { it.isNotBlank() }

    fun setString(key: String, value: String?) {
        if (value == null) settings.remove(prefix(key))
        else settings.putString(prefix(key), value)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        settings.getBoolean(prefix(key), defaultValue)

    fun setBoolean(key: String, value: Boolean) {
        settings.putBoolean(prefix(key), value)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int =
        settings.getLong(prefix(key), defaultValue.toLong()).toInt()

    fun setInt(key: String, value: Int) {
        settings.putLong(prefix(key), value.toLong())
    }

    fun remove(key: String) {
        settings.remove(prefix(key))
    }

    fun clear() {
        // Settings does not expose "all keys"; callers can remove known keys or we could maintain a key set.
        // For now, clear is a no-op unless we add key tracking. Prefer remove(key) for specific keys.
    }

    private fun prefix(key: String): String = KEY_PREFIX + key

    companion object {
        private const val KEY_PREFIX = "app_pref_"
    }
}
