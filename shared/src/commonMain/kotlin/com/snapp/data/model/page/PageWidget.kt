package com.snapp.data.model.page

import com.snapp.data.serializer.ActionsSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class PageWidget(
    val id: String,
    val type: String,
    val title: String? = null,
    @SerialName("description") val desc: String? = null,
    val dataKey: String? = null,
    val schema: String? = null,
    val route: String? = null,
    @Serializable(with = ActionsSerializer::class)
    val actions: List<String>? = null,
    val inlineActions: List<String>? = null,
    val width: Int? = null,
    val height: Int? = null,
    val gridRow: Int? = null,
    val gridColumn: Int? = null,
    val options: JsonObject? = null,
    val components: List<PageWidget>? = null,
    val tabs: List<TabInfo>? = null
) {
    /** Helper for mobile to get column count from options.layout.columns. */
    fun getLayoutColumns(): Int? {
        return options?.get("layout")?.jsonObject?.get("columns")?.jsonPrimitive?.intOrNull
    }

    /** Tabs for tabs widget: from top-level tabs or options.tabs. */
    fun tabsOrFromOptions(): List<TabInfo> {
        if (!tabs.isNullOrEmpty()) return tabs
        return parseTabsFromOptions()
    }

    private fun parseTabsFromOptions(): List<TabInfo> {
        val arr = options?.get("tabs")?.jsonArray ?: return emptyList()
        return arr.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val id = obj["tabId"]?.jsonPrimitive?.content ?: obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val label = obj["label"]?.jsonPrimitive?.content ?: return@mapNotNull null
            TabInfo(id = id, label = label)
        }
    }

    /** options.readOnly — for Card edit/save visibility. */
    fun getOptionsReadOnly(): Boolean? = options?.get("readOnly")?.jsonPrimitive?.booleanOrNull

    /** options.canDelete — for Card delete button. */
    fun getOptionsCanDelete(): Boolean? = options?.get("canDelete")?.jsonPrimitive?.booleanOrNull

    /** options.noShadow — for Card styling. */
    fun getOptionsNoShadow(): Boolean? = options?.get("noShadow")?.jsonPrimitive?.booleanOrNull

    /** For tabs widget: component's tab id (options.tabId or id). */
    fun getTabId(): String = options?.get("tabId")?.jsonPrimitive?.content ?: id

    /** dataKeys of field components that have options.isRequired == true (for Card validation). */
    fun getRequiredFieldDataKeys(): List<String> {
        return (components ?: emptyList())
            .filter { it.type == "field" && it.options?.get("isRequired")?.jsonPrimitive?.booleanOrNull == true }
            .mapNotNull { it.dataKey }
    }
}
