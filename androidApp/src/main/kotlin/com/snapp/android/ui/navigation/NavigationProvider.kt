package com.snapp.android.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

/**
 * Centralized navigation access (React Router / Redux-navigation style).
 * Any composable can navigate without receiving NavController as a parameter.
 * Provide in the root (e.g. MainActivity) with the same NavHostController used by NavHost.
 */
val LocalSnappNavigation = compositionLocalOf<NavHostController> {
    error("LocalSnappNavigation not provided. Wrap your NavHost root with CompositionLocalProvider(LocalSnappNavigation provides navController).")
}
