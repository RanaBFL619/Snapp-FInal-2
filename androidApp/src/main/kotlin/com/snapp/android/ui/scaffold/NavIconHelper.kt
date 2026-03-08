package com.snapp.android.ui.scaffold

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector

internal fun iconForName(name: String?): ImageVector {
    return when (name?.lowercase()) {
        "dashboard", "home" -> Icons.Default.Dashboard
        "account", "accounts", "customer", "customers", "user" -> Icons.Default.AccountCircle
        "person", "profile" -> Icons.Default.Person
        "list", "orders", "records" -> Icons.Default.List
        "users" -> Icons.Default.AccountCircle
        "user-check", "verified" -> Icons.Default.VerifiedUser
        "shopping-cart", "orders", "cart" -> Icons.Default.ShoppingCart
        "dollar-sign", "revenue", "money" -> Icons.Default.AttachMoney
        "settings" -> Icons.Default.Settings
        "logout" -> Icons.Default.Logout
        else -> Icons.Default.MoreHoriz
    }
}

internal fun userMenuIconForName(name: String?): ImageVector {
    return when (name?.lowercase()) {
        "logout" -> Icons.Default.Logout
        "settings" -> Icons.Default.Settings
        "profile", "person", "user" -> Icons.Default.AccountCircle
        else -> Icons.Default.AccountCircle
    }
}
