package com.snapp.android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapp.data.model.page.PageConfig
import com.snapp.data.model.page.PageWidget
import com.snapp.domain.repository.PageRepository
import com.snapp.domain.repository.WidgetDataRepository
import com.snapp.domain.usecase.page.GetPageConfigUseCase
import com.snapp.domain.usecase.widget.GetAllWidgetDataForPageUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.put

class GenericPageViewModel(
    private val getPageConfigUseCase: GetPageConfigUseCase,
    private val getAllWidgetDataForPageUseCase: GetAllWidgetDataForPageUseCase,
    private val pageRepository: PageRepository,
    private val widgetDataRepository: WidgetDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GenericPageUiState())
    private var loadJob: Job? = null
    val uiState: StateFlow<GenericPageUiState> = _uiState.asStateFlow()

    fun loadPage(slug: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = GenericPageUiState(
                isLoading = true,
                activeSlug = slug
            )
            try {
                val pageConfig = getPageConfigUseCase(slug)
                val widgetIds = flattenWidgets(pageConfig.components)
                    .filter { !it.dataKey.isNullOrBlank() }
                    .map { it.id }
                    .toSet()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    page = pageConfig,
                    loadingWidgets = widgetIds
                )
                loadWidgetData(pageConfig)
            } catch (e: Exception) {
                Log.e("GenericPageViewModel", "Error loading page $slug", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load page"
                )
            }
        }
    }

    private fun loadWidgetData(pageConfig: PageConfig) {
        viewModelScope.launch {
            try {
                // Fetch data (shared UseCase returns WidgetDataResult with data keyed by dataKey)
                val result = getAllWidgetDataForPageUseCase(pageConfig)
                val dataMapByDataKey = result.data
                Log.d("GenericPageViewModel", "Fetched data for keys: ${dataMapByDataKey.keys}")
                
                // Remap data to be keyed by widget.id for the UI
                val allWidgets = flattenWidgets(pageConfig.components)
                val dataMapById = allWidgets.mapNotNull { widget ->
                    val data = dataMapByDataKey[widget.dataKey]
                    Log.d("GenericPageViewModel", "Widget: id=${widget.id}, dataKey=${widget.dataKey}, hasData=${data != null}")
                    if (data != null) {
                        Log.v("GenericPageViewModel", "Data for ${widget.id}: $data")
                        widget.id to data
                    } else null
                }.toMap()

                _uiState.value = _uiState.value.copy(
                    widgetData = dataMapById,
                    loadingWidgets = emptySet()
                )
            } catch (e: Exception) {
                Log.e("GenericPageViewModel", "Error loading widget data", e)
                _uiState.value = _uiState.value.copy(
                    loadingWidgets = emptySet()
                )
            }
        }
    }

    fun refreshWidget(widget: PageWidget) {
        val dataKey = widget.dataKey ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loadingWidgets = _uiState.value.loadingWidgets + widget.id
            )
            try {
                val data = widgetDataRepository.getWidgetData(dataKey, null)
                _uiState.value = _uiState.value.copy(
                    widgetData = _uiState.value.widgetData + (widget.id to data),
                    loadingWidgets = _uiState.value.loadingWidgets - widget.id
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loadingWidgets = _uiState.value.loadingWidgets - widget.id
                )
            }
        }
    }

    fun loadTableData(
        widget: PageWidget,
        page: Int,
        pageSize: Int,
        sortField: String?,
        sortDirection: String,
        filters: Map<String, String>
    ) {
        val dataKey = widget.dataKey ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loadingWidgets = _uiState.value.loadingWidgets + widget.id
            )
            try {
                val body = buildJsonObject {
                    put("page", page)
                    put("pageSize", pageSize)
                    put("id", widget.id)
                }
                val data = widgetDataRepository.getWidgetData(dataKey, body)
                _uiState.value = _uiState.value.copy(
                    widgetData = _uiState.value.widgetData + (widget.id to data),
                    loadingWidgets = _uiState.value.loadingWidgets - widget.id
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loadingWidgets = _uiState.value.loadingWidgets - widget.id
                )
            }
        }
    }

    fun createRecord(widget: PageWidget, values: Map<String, String>) {}
    fun updateRecord(widget: PageWidget, recordId: String, values: Map<String, String>) {}
    fun deleteRecord(widget: PageWidget, recordId: String) {}

    private fun flattenWidgets(list: List<PageWidget>): List<PageWidget> {
        return list.flatMap { widget ->
            listOf(widget) + flattenWidgets(widget.components ?: emptyList())
        }
    }

    private fun buildJsonObject(builder: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit): kotlinx.serialization.json.JsonObject {
        return kotlinx.serialization.json.JsonObject(kotlinx.serialization.json.buildJsonObject(builder))
    }
}
