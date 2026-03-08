package com.snapp.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.http.HttpStatusCode

actual fun logHttp(tag: String, message: String) {
    // No-op: never pass Kotlin strings to ObjC logging from a Kotlin/Native context.
}

actual fun createHttpClient(onUnauthorized: () -> Unit): HttpClient = HttpClient(Darwin) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
            if (clientException.response.status == HttpStatusCode.Unauthorized) {
                onUnauthorized()
            }
        }
    }
}

actual fun getBaseUrl(): String = "https://snapp-blue-river.fly.dev"
