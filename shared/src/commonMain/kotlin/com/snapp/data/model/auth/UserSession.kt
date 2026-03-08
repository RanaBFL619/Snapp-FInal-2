package com.snapp.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val token: String,
    val name: String? = null,
    val username: String,
    val roles: List<String> = emptyList(),
    val defaultPage: String? = null
)
