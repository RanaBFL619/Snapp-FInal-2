package com.snapp.data.model.layout

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NavItem(
    val icon: String? = null,
    val label: String,
    val route: String,
    @SerialName("description") val desc: String? = null,
    val children: List<NavItem>? = null
)
