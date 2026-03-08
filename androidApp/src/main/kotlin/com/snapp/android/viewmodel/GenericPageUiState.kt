package com.snapp.android.viewmodel

import com.snapp.data.model.page.PageConfig
import kotlinx.serialization.json.JsonObject

data class GenericPageUiState(
    val isLoading: Boolean = false,
    val page: PageConfig? = null,
    val widgetData: Map<String, JsonObject> = emptyMap(),
    val loadingWidgets: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val activeSlug: String = ""
) {
    val hasData: Boolean get() = page != null && !isLoading
    val isInitialLoading: Boolean get() = isLoading && page == null
}