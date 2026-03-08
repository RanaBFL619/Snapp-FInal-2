package com.snapp.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.snapp.android.ui.navigation.AuthNavigationEffect
import com.snapp.android.ui.navigation.LocalSnappNavigation
import com.snapp.android.ui.navigation.AppRoute
import com.snapp.android.ui.navigation.SnappNavHost
import com.snapp.android.ui.theme.SnappTheme
import com.snapp.android.viewmodel.AuthViewModel
import com.snapp.presentation.state.AuthUiState
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw edge-to-edge; Compose applies safe area padding so content stays below status bar/notch and above nav bar.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on ${thread.name}", throwable)
            throwable.printStackTrace()
            @Suppress("DEPRECATION")
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
        setContent {
            SnappTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    // Central auth store (Koin singleton); UI reacts when authState changes.
                    val authViewModel: AuthViewModel = koinViewModel()
                    val authState by authViewModel.authState.collectAsState()
                    // Route by store: if we have session (from store or just logged in) → auth layout; else → login.
                    val startDestination = if (authState is AuthUiState.Success) AppRoute.Auth.route else AppRoute.Login.route
                    CompositionLocalProvider(LocalSnappNavigation provides navController) {
                        AuthNavigationEffect(navController, authViewModel)
                        SnappNavHost(navController = navController, startDestination = startDestination)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "SnappMainActivity"
    }
}
