package com.snapp.presentation.state

import com.snapp.data.model.page.PageConfig
import kotlinx.serialization.json.JsonObject

/** UI state for generic page screen. See ARCHITECTURE presentation/state/. */
sealed class PageUiState {
    data object Loading : PageUiState()
    data class Success(
        val pageConfig: PageConfig,
        val widgetData: Map<String, JsonObject>,
        val widgetDataFailedKeys: Set<String> = emptySet()
    ) : PageUiState()
    data class Error(val message: String) : PageUiState()
}
