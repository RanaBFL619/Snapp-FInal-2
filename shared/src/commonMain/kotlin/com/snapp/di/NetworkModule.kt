package com.snapp.di

import com.snapp.data.api.SnappApiClient
import com.snapp.data.store.TokenStorage
import com.snapp.data.store.UnauthorizedNotifier
import com.snapp.network.createHttpClient
import com.snapp.network.getBaseUrl
import org.koin.dsl.module

val networkModule = module {
    single { UnauthorizedNotifier() }

    single {
        val tokenStorage = get<TokenStorage>()
        val notifier = get<UnauthorizedNotifier>()
        val client = createHttpClient(onUnauthorized = {
            tokenStorage.clearSession()
            notifier.notifyUnauthorized()
        })
        val api = SnappApiClient(client, getBaseUrl(), tokenStorage)
        notifier.addListener { api.setAuthToken(null) }
        tokenStorage.getToken()?.let { api.setAuthToken(it) }
        api
    }
}
