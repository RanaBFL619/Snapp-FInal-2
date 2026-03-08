package com.snapp.data.model.page

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Tab metadata for tabs widget. API may send "tabId" + "label" or "id" + "label". */
@Serializable
data class TabInfo(
    @SerialName("tabId") val id: String,
    val label: String
)
