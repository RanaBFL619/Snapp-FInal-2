package com.snapp.di

import com.snapp.data.api.SnappApiClient
import com.snapp.data.repository.AuthRepositoryImpl
import com.snapp.data.repository.LayoutRepositoryImpl
import com.snapp.data.repository.PageRepositoryImpl
import com.snapp.data.repository.RecordRepositoryImpl
import com.snapp.data.repository.WidgetDataRepositoryImpl
import com.snapp.data.store.TokenStorage
import com.snapp.domain.repository.AuthRepository
import com.snapp.domain.repository.LayoutRepository
import com.snapp.domain.repository.PageRepository
import com.snapp.domain.repository.RecordRepository
import com.snapp.domain.repository.WidgetDataRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get<SnappApiClient>(), get<TokenStorage>()) }
    single<LayoutRepository> { LayoutRepositoryImpl(get<SnappApiClient>(), get<TokenStorage>()) }
    single<PageRepository> { PageRepositoryImpl(get<SnappApiClient>()) }
    single<WidgetDataRepository> { WidgetDataRepositoryImpl(get<SnappApiClient>()) }
    single<RecordRepository> { RecordRepositoryImpl(get<SnappApiClient>()) }
}
