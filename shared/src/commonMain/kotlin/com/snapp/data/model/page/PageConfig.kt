package com.snapp.data.model.page

import kotlinx.serialization.Serializable

@Serializable
data class PageConfig(
    val title: String,
    val layout: PageLayout? = null,
    val components: List<PageWidget> = emptyList()
)
