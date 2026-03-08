package com.snapp.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class LostPasswordRequest(
    val email: String
)
