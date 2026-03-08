package com.snapp.android.ui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object ForgotPassword : AppRoute("forgot-password")
    data object ResetPassword : AppRoute("reset-password/{token}") {
        fun createRoute(token: String) = "reset-password/$token"
    }
    /** Single entry for all authenticated screens; scaffold wraps inner NavHost. */
    data object Auth : AppRoute("auth")
    /** Dynamic entity route (matches web :entity): /dashboard, /customers, /orders, etc. Any slug from layout API. */
    data class Entity(val slug: String) : AppRoute(slug)
    data class Record(val id: String) : AppRoute("record/$id")
    data object Reports : AppRoute("reports")

    companion object {
        private const val ROUTE_LOGIN = "login"
        private const val ROUTE_FORGOT_PASSWORD = "forgot-password"

        /** Routes that do not require authentication (exact or pattern). */
        val unauthenticatedRoutes: Set<String> = setOf(ROUTE_LOGIN, ROUTE_FORGOT_PASSWORD)

        /** True if [route] is an unauthenticated destination (e.g. login, forgot-password, reset-password/xxx). */
        fun isUnauthenticatedRoute(route: String?): Boolean = when {
            route == null -> true
            route == ROUTE_LOGIN -> true
            route == ROUTE_FORGOT_PASSWORD -> true
            route.startsWith("reset-password/") -> true
            else -> false
        }

        /** Normalize API route to entity slug (matches web). Backend may send "/customer", "/page/orders", or "dashboard". */
        fun normalizeEntitySlug(route: String): String =
            route.removePrefix("/").removePrefix("page/").trim().ifEmpty { "dashboard" }
    }
}
