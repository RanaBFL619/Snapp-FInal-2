package com.snapp.android.di

import com.snapp.android.viewmodel.AuthViewModel
import com.snapp.android.viewmodel.GenericPageViewModel
import com.snapp.android.viewmodel.LayoutViewModel
import com.snapp.android.viewmodel.RecordViewModel
import com.snapp.presentation.viewmodel.LayoutSharedViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val AndroidModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel {
        GenericPageViewModel(
            getPageConfigUseCase = get(),
            getAllWidgetDataForPageUseCase = get(),
            pageRepository = get(),
            widgetDataRepository = get()
        )
    }
    viewModel { LayoutViewModel(get<LayoutSharedViewModel>()) }
    viewModel {
        RecordViewModel(
            recordRepository = get(),
            widgetDataRepository = get()
        )
    }
}