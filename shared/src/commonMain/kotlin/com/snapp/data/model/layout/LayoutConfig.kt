package com.snapp.data.model.layout

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LayoutConfig(
    val navbar: NavbarConfig? = null,
    val footer: FooterConfig? = null,
    val theme: JsonObject? = null
)
