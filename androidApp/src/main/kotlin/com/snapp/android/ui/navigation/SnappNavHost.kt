package com.snapp.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController

/**
 * Root NavHost. Auth-based navigation is handled by [AuthNavigationEffect] in the parent.
 * [startDestination] should be [AppRoute.Auth.route] when session is restored so the user
 * doesn't see Login briefly; otherwise [AppRoute.Login.route].
 */
@Composable
fun SnappNavHost(
    navController: NavHostController,
    startDestination: String = AppRoute.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        snappNavGraph(navController)
    }
}
