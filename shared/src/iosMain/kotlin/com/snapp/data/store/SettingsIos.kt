package com.snapp.data.store

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

actual fun createSettings(platformContext: Any?): com.russhwolf.settings.Settings {
    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
