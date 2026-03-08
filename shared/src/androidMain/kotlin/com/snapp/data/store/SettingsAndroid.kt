package com.snapp.data.store

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(platformContext: Any?): com.russhwolf.settings.Settings {
    val context = platformContext as? Context ?: throw IllegalArgumentException("Context required on Android")
    val prefs = context.applicationContext.getSharedPreferences("snapp", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}
