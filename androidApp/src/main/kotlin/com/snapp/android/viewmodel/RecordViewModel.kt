package com.snapp.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapp.data.model.page.PageWidget
import com.snapp.domain.repository.RecordRepository
import com.snapp.domain.repository.WidgetDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
data class RecordUiState(
    val isLoading: Boolean = false,
    val recordId: String = "",
    val recordData: JsonObject? = null,
    val widgetData: Map<String, JsonObject> = emptyMap(),
    val loadingWidgets: Set<String> = emptySet(),
    val errorMessage: String? = null
) {
    val hasData: Boolean get() = recordData != null && !isLoading
    val isInitialLoading: Boolean get() = isLoading && recordData == null
}
class RecordViewModel(
    private val recordRepository: RecordRepository,
    private val widgetDataRepository: WidgetDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()
    fun loadRecord(recordId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                recordId = recordId
            )
            try {
                val recordData = recordRepository.getRecord(recordId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    recordData = recordData
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load record"
                )
            }
        }
    }
    fun refreshWidget(widget: PageWidget) {
        val dataKey = widget.dataKey ?: return
        val recordId = _uiState.value.recordId
        if (recordId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loadingWidgets = _uiState.value.loadingWidgets + widget.id
            )
            try {
                val body = buildJsonObject {
                    put("recordId", recordId)
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
    fun loadTableData(
        widget: PageWidget,
        page: Int,
        pageSize: Int,
        sortField: String?,
        sortDirection: String,
        filters: Map<String, String>
    ) {
        val dataKey = widget.dataKey ?: return
        val recordId = _uiState.value.recordId
        if (recordId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loadingWidgets = _uiState.value.loadingWidgets + widget.id
            )
            try {
                val body = buildJsonObject {
                    put("page", page)
                    put("pageSize", pageSize)
                    put("id", widget.id)
                    put("recordId", recordId)
                    // Add sorting and filters
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
    // CRUD operations
    fun createRecord(schemaType: String, values: Map<String, String>) {
        viewModelScope.launch {
            try {
                // Implementation based on your API
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }
    fun updateRecord(schemaType: String, recordId: String, values: Map<String, String>) {
        viewModelScope.launch {
            try {
                // Implementation based on your API
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }
    fun deleteRecord(schemaType: String, recordId: String) {
        viewModelScope.launch {
            try {
                // Implementation based on your API
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }
}
