package com.snapp.data.api.request

import com.snapp.data.model.widget.TableSort
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TableRequest(
    val page: Int,
    val pageSize: Int,
    val sort: List<TableSort>? = null,
    val filters: JsonObject? = null,
    val search: String? = null,
    val id: String? = null,
    val recordId: String? = null
)
