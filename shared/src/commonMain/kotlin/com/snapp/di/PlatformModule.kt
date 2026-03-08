package com.snapp.di

import com.snapp.data.store.AppPreferenceStore
import com.snapp.data.store.TokenStorage
import com.snapp.data.store.createSettings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun platformModule(platformContext: Any?) = module {
    single { createSettings(platformContext) }
    singleOf(::TokenStorage)
    single { AppPreferenceStore(get()) }
}
