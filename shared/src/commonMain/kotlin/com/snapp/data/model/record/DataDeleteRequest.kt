package com.snapp.data.model.record

import kotlinx.serialization.Serializable

@Serializable
data class DataDeleteRequest(
    val schemaType: String,
    val recordIds: List<String>
)
