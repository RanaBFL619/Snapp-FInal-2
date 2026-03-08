package com.snapp.domain.repository

import com.snapp.data.model.auth.LoginResponse
import com.snapp.data.model.auth.UserSession

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponse>
    suspend fun logout()
    fun getToken(): String?
    fun getSession(): UserSession?
}
