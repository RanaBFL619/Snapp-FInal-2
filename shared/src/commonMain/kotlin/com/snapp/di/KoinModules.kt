package com.snapp.di

import org.koin.core.module.Module

val sharedKoinModules: (platformContext: Any?) -> List<Module> = { platformContext ->
    listOf(
        platformModule(platformContext),
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule
    )
}
