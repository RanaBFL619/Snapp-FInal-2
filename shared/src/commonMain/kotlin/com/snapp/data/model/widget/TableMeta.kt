package com.snapp.data.model.widget

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TableMeta(
    val totalRecords: Int,
    val totalPages: Int = 0,
    @SerialName("page")
    val currentPage: Int = 0,
    val pageSize: Int
)
