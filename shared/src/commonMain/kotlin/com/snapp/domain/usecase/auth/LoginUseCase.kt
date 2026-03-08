package com.snapp.domain.usecase.auth

import com.snapp.data.model.auth.LoginResponse
import com.snapp.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<LoginResponse> {
        return authRepository.login(username, password)
    }
}
