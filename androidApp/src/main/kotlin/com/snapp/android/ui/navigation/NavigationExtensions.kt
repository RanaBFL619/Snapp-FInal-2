package com.snapp.android.ui.navigation

import androidx.navigation.NavHostController

/** Navigate to entity page (matches web :entity). Use for any slug from layout API: dashboard, customers, orders, etc. */
fun NavHostController.navigateToEntity(slug: String) {
    navigate(slug) {
        launchSingleTop = true
        popUpTo("dashboard") { inclusive = (slug == "dashboard") }
    }
}

/** @deprecated Use navigateToEntity(slug) for parity with web. */
fun NavHostController.navigateToPage(slug: String) = navigateToEntity(slug)

fun NavHostController.navigateToRecord(id: String) {
    navigate("record/$id") {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToReports() {
    navigate(AppRoute.Reports.route) {
        launchSingleTop = true
    }
}
