package com.snapp.network

import io.ktor.client.HttpClient

/**
 * Creates HttpClient with 401 handling: when any response is 401 Unauthorized,
 * [onUnauthorized] is invoked (clear token storage, notify listeners) and the request fails.
 * Matches web apiClient: token on every request, on 401 clear store and show login.
 */
expect fun createHttpClient(onUnauthorized: () -> Unit): HttpClient
