package com.snapp.network

import kotlinx.serialization.json.Json

/**
 * Single application-wide Json instance used for all HTTP request/response serialization.
 * Defined here in commonMain so it is created once and shared across all platforms.
 */
val sharedJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
