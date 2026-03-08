package com.snapp.presentation.state

import com.snapp.data.model.page.PageConfig

/**
 * Swift-friendly representation of page state.
 * widgetData stores JSON strings (serialized from JsonObject) so the Map<String, String>
 * bridges cleanly across the KMP Swift boundary without opaque Kotlin types.
 * widgetDataFailedKeys: dataKeys whose API call failed — show error only for those widgets (ErrorBoundary-style).
 */
data class PageUiStateSnapshot(
    val kind: String,
    val slug: String? = null,
    val pageConfig: PageConfig? = null,
    val widgetData: Map<String, String>? = null,
    val widgetDataFailedKeys: List<String>? = null,
    val errorMessage: String? = null
) {
    companion object {
        const val KIND_LOADING = "loading"
        const val KIND_SUCCESS = "success"
        const val KIND_ERROR = "error"
    }
}
