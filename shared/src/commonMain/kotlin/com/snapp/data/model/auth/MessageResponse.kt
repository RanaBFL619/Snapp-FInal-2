package com.snapp.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String? = null,
    val success: Boolean? = null
)
