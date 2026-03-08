package com.snapp.data.model.layout

import kotlinx.serialization.Serializable

@Serializable
data class FooterLink(
    val label: String,
    val route: String
)

@Serializable
data class FooterConfig(
    val text: String = "",
    val links: List<FooterLink> = emptyList()
)
