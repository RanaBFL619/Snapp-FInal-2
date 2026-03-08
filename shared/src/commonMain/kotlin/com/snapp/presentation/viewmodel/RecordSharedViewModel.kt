package com.snapp.presentation.viewmodel

import com.snapp.presentation.state.RecordUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Shared VM for record detail. See ARCHITECTURE presentation/viewmodel/. */
class RecordSharedViewModel {
    private val _recordUiState = MutableStateFlow<RecordUiState>(RecordUiState.Loading)
    val recordUiState: StateFlow<RecordUiState> = _recordUiState.asStateFlow()
}
