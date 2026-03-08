package com.snapp.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val password: String
)
