package com.snapp.data.repository

import com.snapp.data.api.SnappApiClient
import com.snapp.data.api.request.TableRequest
import com.snapp.data.model.widget.TableResponse
import com.snapp.domain.repository.WidgetDataRepository
import kotlinx.serialization.json.JsonObject

class WidgetDataRepositoryImpl(
    private val api: SnappApiClient
) : WidgetDataRepository {

    override suspend fun getWidgetData(dataKey: String, body: JsonObject?): JsonObject =
        api.getWidgetData(dataKey, body)

    override suspend fun getTableData(dataKey: String, request: TableRequest): TableResponse =
        api.getTableWidgetData(dataKey, request)
}
