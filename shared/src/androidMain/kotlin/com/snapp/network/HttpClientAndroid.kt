package com.snapp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

actual fun logHttp(tag: String, message: String) {
    android.util.Log.d(tag, message)
}

actual fun createHttpClient(onUnauthorized: () -> Unit): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
            if (clientException.response.status == HttpStatusCode.Unauthorized) {
                onUnauthorized()
            }
        }
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                logHttp("HttpClient", message)
            }
        }
        level = LogLevel.BODY
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
    engine {
        config { followRedirects(true) }
    }
}

actual fun getBaseUrl(): String = "https://snapp-blue-river.fly.dev"
