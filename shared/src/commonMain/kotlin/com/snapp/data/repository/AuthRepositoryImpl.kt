package com.snapp.data.repository

import com.snapp.data.api.SnappApiClient
import com.snapp.data.model.auth.LoginRequest
import com.snapp.data.model.auth.LoginResponse
import com.snapp.data.model.auth.UserSession
import com.snapp.data.store.TokenStorage
import com.snapp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: SnappApiClient,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoginResponse> = runCatching {
        val response = api.login(LoginRequest(username = username, password = password))
        tokenStorage.saveSession(
            UserSession(
                token = response.token,
                name = response.name,
                username = response.username,
                roles = response.roles ?: emptyList(),
                defaultPage = response.defaultPage
            )
        )
        api.setAuthToken(response.token)
        response
    }

    override suspend fun logout() {
        runCatching { api.logout() }
        tokenStorage.clearSession()
        api.setAuthToken(null)
    }

    override fun getToken(): String? = tokenStorage.getToken() ?: api.getAuthToken()

    override fun getSession(): UserSession? = tokenStorage.getSession()
}
