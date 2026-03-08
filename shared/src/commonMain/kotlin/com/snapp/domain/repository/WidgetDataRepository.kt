package com.snapp.domain.repository

import com.snapp.data.api.request.TableRequest
import com.snapp.data.model.widget.TableResponse
import kotlinx.serialization.json.JsonObject

interface WidgetDataRepository {
    suspend fun getWidgetData(dataKey: String, body: JsonObject? = null): JsonObject
    suspend fun getTableData(dataKey: String, request: TableRequest): TableResponse
}
