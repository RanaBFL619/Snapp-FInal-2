package com.snapp.data.model.widget

import kotlinx.serialization.Serializable

@Serializable
data class TableSort(
    val field: String,
    val direction: String
)
