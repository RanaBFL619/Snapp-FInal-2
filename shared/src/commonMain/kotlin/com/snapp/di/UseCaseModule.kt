package com.snapp.di

import com.snapp.domain.usecase.auth.LoginUseCase
import com.snapp.domain.usecase.auth.LogoutUseCase
import com.snapp.domain.usecase.layout.GetLayoutUseCase
import com.snapp.domain.usecase.page.CollectWidgetDataKeysUseCase
import com.snapp.domain.usecase.page.GetPageConfigUseCase
import com.snapp.domain.usecase.widget.GetAllWidgetDataForPageUseCase
import com.snapp.domain.repository.WidgetDataRepository
import org.koin.dsl.module

val useCaseModule = module {
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { GetLayoutUseCase(get()) }
    single { GetPageConfigUseCase(get()) }
    single { CollectWidgetDataKeysUseCase() }
    single { GetAllWidgetDataForPageUseCase(get<CollectWidgetDataKeysUseCase>(), get<WidgetDataRepository>()) }
}
