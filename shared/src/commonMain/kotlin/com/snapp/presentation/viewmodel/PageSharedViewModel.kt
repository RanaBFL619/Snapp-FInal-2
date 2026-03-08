package com.snapp.presentation.viewmodel

import com.snapp.data.model.page.PageConfig
import com.snapp.domain.usecase.page.GetPageConfigUseCase
import com.snapp.presentation.state.PageUiState
import com.snapp.presentation.state.PageUiStateSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** Shared VM for generic page. See ARCHITECTURE presentation/viewmodel/. */
class PageSharedViewModel(
    private val scope: CoroutineScope,
    private val getPageConfigUseCase: GetPageConfigUseCase,
) {
    private val _pageUiState = MutableStateFlow<PageUiState>(PageUiState.Loading)
    val pageUiState: StateFlow<PageUiState> = _pageUiState.asStateFlow()

    /** Slug for the last loadPage call — included in snapshots so each screen only applies its own data. */
    private var currentSlug: String = ""

    /**
     * Load page for route param (e.g. dashboard, customer, projects).
     * Only fetches page config; widget data is fetched by individual widgets (later).
     */
    suspend fun loadPage(slug: String) {
        currentSlug = slug
        _pageUiState.value = PageUiState.Loading
        val pageConfig = try {
            println("[Snapp] Page load: GET /meta/page/$slug")
            val config = getPageConfigUseCase(slug)
            println("[Snapp] Page load success: slug=$slug | title=${config.title} | components=${config.components.size}")
            config
        } catch (e: Throwable) {
            println("[Snapp] Page config failed | slug=$slug | error=${e.message}")
            e.printStackTrace()
            PageConfig(title = "", components = emptyList())
        }
        _pageUiState.value = PageUiState.Success(
            pageConfig = pageConfig,
            widgetData = emptyMap(),
            widgetDataFailedKeys = emptySet()
        )
    }

    /** iOS StateFlow observation bridge — collects state as PageUiStateSnapshot.
     *  widgetData values are serialized to JSON strings so they bridge cleanly to Swift. */
    fun collectPageStateSnapshot(onState: (PageUiStateSnapshot) -> Unit) {
        scope.launch {
            pageUiState.collect { state ->
                onState(
                    when (state) {
                        is PageUiState.Success -> PageUiStateSnapshot(
                            kind = PageUiStateSnapshot.KIND_SUCCESS,
                            slug = currentSlug,
                            pageConfig = state.pageConfig,
                            widgetData = state.widgetData.mapValues { (_, jsonObj) ->
                                Json.encodeToString(JsonObject.serializer(), jsonObj)
                            },
                            widgetDataFailedKeys = state.widgetDataFailedKeys.toList()
                        )
                        is PageUiState.Error -> PageUiStateSnapshot(
                            kind = PageUiStateSnapshot.KIND_ERROR,
                            slug = currentSlug,
                            errorMessage = state.message
                        )
                        else -> PageUiStateSnapshot(
                            kind = PageUiStateSnapshot.KIND_LOADING,
                            slug = currentSlug
                        )
                    }
                )
            }
        }
    }
}
