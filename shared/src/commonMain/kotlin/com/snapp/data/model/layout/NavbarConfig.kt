package com.snapp.data.model.layout

import kotlinx.serialization.Serializable

@Serializable
data class NavbarUserMenuItem(
    val icon: String? = null,
    val label: String,
    val route: String
)

@Serializable
data class NavbarConfig(
    val location: String? = null,
    val logoText: String = "",
    val logoUrl: String = "",
    val nav: List<NavItem> = emptyList(),
    val userMenu: List<NavbarUserMenuItem> = emptyList()
)
