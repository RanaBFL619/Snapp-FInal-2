package com.snapp.android.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.snapp.android.ui.navigation.AppRoute
import com.snapp.android.ui.navigation.LocalSnappNavigation
import com.snapp.android.ui.scaffold.skeleton.SkeletonShell
import com.snapp.android.viewmodel.AuthViewModel
import com.snapp.android.viewmodel.LayoutViewModel
import com.snapp.data.model.layout.NavItem
import com.snapp.data.model.layout.NavbarUserMenuItem
import com.snapp.presentation.state.LayoutUiState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// Same gray as header and bottom bar (Figma #E8E8E8); used for status/nav bar fillers.
private val HeaderBottomBarColor = androidx.compose.ui.graphics.Color(0xFFE8E8E8)

@Composable
fun AppScaffold(
    currentRoute: String,
    onLogout: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val layoutViewModel: LayoutViewModel = koinViewModel()
    val authViewModel: AuthViewModel = koinViewModel()
    val layoutState by layoutViewModel.layoutState.collectAsState()
    val userSession by authViewModel.userSession.collectAsState()

    // Layout API only when auth store has session (same as web). Dynamic nav/userMenu/logo from API.
    LaunchedEffect(userSession) {
        if (userSession != null) {
            layoutViewModel.loadLayout()
        }
    }

    val layout = when (val state = layoutState) {
        is LayoutUiState.Loading -> null
        is LayoutUiState.Error -> null
        is LayoutUiState.Success -> state.layout
    }

    if (layout == null) {
        SkeletonShell()
        return
    }

    val navbar = layout.navbar
            val navItems = navbar?.nav ?: emptyList()
            val logoText = navbar?.logoText ?: ""
            val logoUrl = navbar?.logoUrl ?: ""
            val userMenuItems = navbar?.userMenu ?: emptyList()
            val userName = userSession?.name ?: userSession?.username ?: ""
            val userEmail = userSession?.username ?: ""

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val navController = LocalSnappNavigation.current
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(layoutState) {
                if (layoutState is LayoutUiState.Error) {
                    val result = snackbarHostState.showSnackbar(
                        message = "Failed to load layout. Tap Retry to load from server.",
                        actionLabel = "Retry"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        layoutViewModel.loadLayout()
                    }
                }
            }

            val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
            val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

            val onNavItemClick: (NavItem) -> Unit = { item ->
                closeDrawer()
                val slug = AppRoute.normalizeEntitySlug(item.route)
                val route = if (slug == "reports") AppRoute.Reports.route else slug
                navController.navigate(route) {
                    launchSingleTop = true
                    popUpTo("dashboard") { inclusive = (route == "dashboard") }
                }
            }

            val onUserMenuItemClick: (NavbarUserMenuItem) -> Unit = { item ->
                navController.navigate(AppRoute.normalizeEntitySlug(item.route)) {
                    launchSingleTop = true
                }
            }

            Column(Modifier.fillMaxSize()) {
                // Extend header color into status bar so no gap at top
                Box(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(HeaderBottomBarColor)
                )
                ModalNavigationDrawer(
                    modifier = Modifier.weight(1f),
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        NavigationDrawerContent(
                            navItems = navItems,
                            currentRoute = currentRoute,
                            logoText = logoText,
                            logoUrl = logoUrl,
                            userName = userName,
                            userEmail = userEmail,
                            onItemClick = onNavItemClick,
                            onClose = closeDrawer
                        )
                    }
                ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        SnappTopBar(
                            title = logoText,
                            logoUrl = logoUrl,
                            userName = userName,
                            userMenuItems = userMenuItems,
                            onMenuClick = openDrawer,
                            onUserMenuItemClick = onUserMenuItemClick,
                            onLogout = onLogout ?: { authViewModel.logout() }
                        )
                    },
                    bottomBar = {
                        if (navItems.isNotEmpty()) {
                            SnappBottomNavBar(
                                navItems = navItems,
                                currentRoute = currentRoute,
                                onItemClick = onNavItemClick,
                                onMoreClick = openDrawer
                            )
                        }
                    },
                    containerColor = androidx.compose.ui.graphics.Color.White
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(androidx.compose.ui.graphics.Color.White)
                    ) {
                        content()
                }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                        .background(HeaderBottomBarColor)
                )
            }
    }
}
