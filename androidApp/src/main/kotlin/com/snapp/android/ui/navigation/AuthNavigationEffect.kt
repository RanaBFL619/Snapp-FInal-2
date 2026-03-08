package com.snapp.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.snapp.android.viewmodel.AuthViewModel
import com.snapp.presentation.state.AuthUiState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * Handles auth-state-driven navigation from **outside** the NavHost so we never
 * call navigate() from inside the host (avoids crashes on successful login).
 * Place this as a sibling to SnappNavHost in the composition tree.
 */
@Composable
fun AuthNavigationEffect(
    navController: NavHostController,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                val currentDest = navController.currentDestination?.route
                if (currentDest == AppRoute.Login.route || currentDest == AppRoute.ForgotPassword.route) {
                    delay(150)
                    try {
                        navController.navigate(AppRoute.Auth.route) {
                            launchSingleTop = true
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                        }
                    } catch (t: Throwable) {
                        android.util.Log.e("AuthNav", "Navigate to auth failed", t)
                    }
                }
            }
            is AuthUiState.Idle -> {
                val currentDest = navController.currentDestination?.route
                if (!AppRoute.isUnauthenticatedRoute(currentDest)) {
                    delay(100)
                    try {
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (_: Throwable) { }
                }
            }
            else -> {}
        }
    }
}
