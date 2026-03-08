package com.snapp.domain.usecase.widget

import com.snapp.data.model.page.PageConfig
import com.snapp.data.api.request.TableRequest
import com.snapp.data.model.widget.TableResponse
import com.snapp.domain.repository.WidgetDataRepository
import com.snapp.domain.usecase.page.CollectWidgetDataKeysUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Result of loading widget data for a page. Failed keys get empty data so only those widgets show error (ErrorBoundary-style). */
data class WidgetDataResult(
    val data: Map<String, JsonObject>,
    val failedKeys: Set<String> = emptySet()
)

class GetAllWidgetDataForPageUseCase(
    private val collectWidgetDataKeysUseCase: CollectWidgetDataKeysUseCase,
    private val widgetDataRepository: WidgetDataRepository
) {
    suspend operator fun invoke(pageConfig: PageConfig): WidgetDataResult = coroutineScope {
        val pairs = collectWidgetDataKeysUseCase(pageConfig.components)
        val results = pairs.map { (widget, isTable) ->
            async {
                val dataKey = widget.dataKey ?: return@async Triple("", buildJsonObject { }, false)
                val (data, failed) = try {
                    if (isTable) {
                        val tableResponse = widgetDataRepository.getTableData(
                            dataKey,
                            TableRequest(page = 1, pageSize = 10)
                        )
                        tableResponseToJsonObject(tableResponse) to false
                    } else {
                        widgetDataRepository.getWidgetData(dataKey, null) to false
                    }
                } catch (_: Exception) {
                    buildJsonObject { } to true
                }
                Triple(dataKey, data, failed)
            }
        }.awaitAll()
        val data = results.filter { it.first.isNotBlank() }.associate { it.first to it.second }
        val failedKeys = results.filter { it.third }.map { it.first }.toSet()
        WidgetDataResult(data = data, failedKeys = failedKeys)
    }

    private fun tableResponseToJsonObject(response: TableResponse): JsonObject = buildJsonObject {
        putJsonArray("records") { response.records.forEach { add(it) } }
        putJsonObject("meta") {
            put("totalRecords", response.meta.totalRecords)
            put("totalPages", response.meta.totalPages)
            put("page", response.meta.currentPage)
            put("pageSize", response.meta.pageSize)
        }
    }
}
