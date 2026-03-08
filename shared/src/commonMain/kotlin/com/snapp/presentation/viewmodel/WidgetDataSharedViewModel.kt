package com.snapp.presentation.viewmodel

import com.snapp.presentation.state.WidgetUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Shared VM for widget data. See ARCHITECTURE presentation/viewmodel/. */
class WidgetDataSharedViewModel {
    private val _state = MutableStateFlow<WidgetUiState>(WidgetUiState.Loading)
    val state: StateFlow<WidgetUiState> = _state.asStateFlow()
}
