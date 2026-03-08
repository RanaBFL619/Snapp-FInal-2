package com.snapp.di

import com.snapp.data.api.SnappApiClient
import com.snapp.data.model.auth.UserSession
import com.snapp.data.store.TokenStorage
import com.snapp.data.store.UnauthorizedNotifier
import com.snapp.domain.repository.AuthRepository
import com.snapp.presentation.viewmodel.AuthSharedViewModel
import com.snapp.presentation.viewmodel.LayoutSharedViewModel
import com.snapp.presentation.viewmodel.PageSharedViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

private var koinApp: KoinApplication? = null

object SnappKoin {
    fun doInitKoin(platformContext: Any? = null) {
        koinApp = startKoin {
            modules(sharedKoinModules(platformContext))
        }
    }

    fun getAuthViewModel(): AuthSharedViewModel = koinApp!!.koin.get<AuthSharedViewModel>()

    fun getLayoutViewModel(): LayoutSharedViewModel = koinApp!!.koin.get<LayoutSharedViewModel>()

    fun getPageSharedViewModel(): PageSharedViewModel = koinApp!!.koin.get<PageSharedViewModel>()

    /** Returns stored session if any (for iOS sync restore so we don't flash Login). Call after doInitKoin. */
    fun getStoredSession(): UserSession? = koinApp!!.koin.get<AuthRepository>().getSession()

    /** Call on main thread at app startup (e.g. iOS) so HttpClient is created on main thread and avoids EXC_BAD_ACCESS. */
    fun warmUpNetwork() {
        koinApp!!.koin.get<SnappApiClient>()
    }

    /** Call when any API returns 401 (e.g. native iOS layout call). Clears token storage and notifies listeners so UI shows login. Same as web. */
    fun clearSessionDueToUnauthorized() {
        koinApp!!.koin.get<TokenStorage>().clearSession()
        koinApp!!.koin.get<UnauthorizedNotifier>().notifyUnauthorized()
    }
}

fun initKoin(platformContext: Any? = null) {
    SnappKoin.doInitKoin(platformContext)
}

fun getAuthViewModel(): AuthSharedViewModel = SnappKoin.getAuthViewModel()

fun getLayoutViewModel(): LayoutSharedViewModel = SnappKoin.getLayoutViewModel()

/** Returns stored session if any (for iOS sync restore). Call after initKoin. */
fun getStoredSession(): UserSession? = SnappKoin.getStoredSession()

/** Call when any API returns 401 (e.g. native iOS layout). Clears storage and notifies so UI shows login. Exposed as top-level for Swift. */
fun clearSessionDueToUnauthorized() = SnappKoin.clearSessionDueToUnauthorized()
