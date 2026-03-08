package com.snapp.presentation.state

import com.snapp.data.model.layout.LayoutConfig

/**
 * Swift-friendly snapshot of layout state. Use from iOS instead of
 * casting LayoutUiState sealed subclasses.
 */
data class LayoutStateSnapshot(
    val kind: String,
    val layout: LayoutConfig? = null,
    val errorMessage: String? = null
) {
    companion object {
        const val KIND_LOADING = "loading"
        const val KIND_SUCCESS = "success"
        const val KIND_ERROR = "error"
    }
}
