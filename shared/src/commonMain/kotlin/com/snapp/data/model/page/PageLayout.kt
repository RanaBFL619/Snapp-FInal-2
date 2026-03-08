package com.snapp.data.model.page

import kotlinx.serialization.Serializable

@Serializable
data class PageLayout(
    val type: String? = null,
    val columns: Int = 4,
    val rowHeight: Int? = null,
    val gutter: Int? = null
)
