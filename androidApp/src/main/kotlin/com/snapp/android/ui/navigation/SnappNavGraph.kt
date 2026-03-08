package com.snapp.android.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.CompositionLocalProvider
import com.snapp.android.ui.scaffold.AppScaffold
import com.snapp.android.ui.screen.generic.GenericPageScreen
import com.snapp.android.ui.screen.login.ForgotPasswordScreen
import com.snapp.android.ui.screen.login.LoginScreen
import com.snapp.android.ui.screen.login.ResetPasswordScreen
import com.snapp.android.ui.screen.record.RecordDetailScreen
import com.snapp.android.ui.screen.reports.ReportsScreen
import org.koin.androidx.compose.koinViewModel
import com.snapp.android.viewmodel.AuthViewModel

fun NavGraphBuilder.snappNavGraph(navController: NavHostController) {
    // --- Unauthenticated routes (no scaffold); pad so content stays in safe area ---
    composable(AppRoute.Login.route) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            LoginScreen(onForgotPassword = { navController.navigate(AppRoute.ForgotPassword.route) })
        }
    }
    composable(AppRoute.ForgotPassword.route) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }
    }
    composable(
        route = AppRoute.ResetPassword.route,
        arguments = listOf(navArgument("token") { type = NavType.StringType; defaultValue = "" })
    ) { backStackEntry ->
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            ResetPasswordScreen(token = backStackEntry.arguments?.getString("token") ?: "")
        }
    }

    // --- Single authenticated shell: one scaffold for all auth routes ---
    composable(AppRoute.Auth.route) {
        val authViewModel: AuthViewModel = koinViewModel()
        val innerNavController = rememberNavController()
        val innerBackStackEntry by innerNavController.currentBackStackEntryAsState()
        val currentRoute = innerBackStackEntry?.let { entry ->
            val r = entry.destination.route
            when {
                r == AppRoute.Reports.route -> AppRoute.Reports.route
                r?.startsWith("record/") == true -> "record/${entry.arguments?.getString("id") ?: ""}"
                else -> entry.arguments?.getString("entity") ?: r ?: "dashboard"
            }
        } ?: "dashboard"

        CompositionLocalProvider(LocalSnappNavigation provides innerNavController) {
            AppScaffold(
                currentRoute = currentRoute,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            ) {
                NavHost(
                    navController = innerNavController,
                    startDestination = "dashboard"
                ) {
                    composable(
                        route = "record/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        RecordDetailScreen(recordId = id)
                    }
                    composable(AppRoute.Reports.route) {
                        ReportsScreen()
                    }
                    composable(
                        route = "{entity}",
                        arguments = listOf(navArgument("entity") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val entity = backStackEntry.arguments?.getString("entity") ?: "dashboard"
                        GenericPageScreen(slug = entity)
                    }
                }
            }
        }
    }
}
