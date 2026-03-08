package com.snapp.data.model.record

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DataInsertRequest(
    val schemaType: String,
    val records: List<JsonObject>
)
