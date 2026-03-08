package com.snapp.data.model.widget

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TableResponse(
    val records: List<JsonObject>,
    val meta: TableMeta
)
