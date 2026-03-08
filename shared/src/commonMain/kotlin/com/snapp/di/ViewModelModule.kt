package com.snapp.di

import com.snapp.domain.usecase.layout.GetLayoutUseCase
import com.snapp.presentation.viewmodel.AuthSharedViewModel
import com.snapp.presentation.viewmodel.LayoutSharedViewModel
import com.snapp.presentation.viewmodel.PageSharedViewModel
import com.snapp.presentation.viewmodel.RecordSharedViewModel
import com.snapp.presentation.viewmodel.WidgetDataSharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val viewModelModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single { AuthSharedViewModel(get(), get(), get(), get(), get()) }
    single { LayoutSharedViewModel(get<GetLayoutUseCase>(), get<CoroutineScope>()) }
    single { PageSharedViewModel(get(), get()) }
    single { WidgetDataSharedViewModel() }
    single { RecordSharedViewModel() }
}
