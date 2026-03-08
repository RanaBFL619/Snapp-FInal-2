package com.snapp.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapp.presentation.state.LayoutUiState
import com.snapp.presentation.viewmodel.LayoutSharedViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LayoutViewModel(
    private val shared: LayoutSharedViewModel
) : ViewModel() {

    val layoutState: StateFlow<LayoutUiState> =
        shared.layoutState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LayoutUiState.Loading
        )

    fun loadLayout() = shared.loadLayout()
}
