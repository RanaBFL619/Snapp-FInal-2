package com.snapp.android.ui.widget.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snapp.android.ui.scaffold.iconForName
import com.snapp.android.ui.widget.WidgetRenderContext
import com.snapp.data.model.page.PageWidget
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun MetricWidget(context: WidgetRenderContext) {
    val widget = context.widget
    val data = context.widgetData as? JsonElement

    val value = extractMetricValue(data, widget)
    val iconName = widget.options?.get("icon")?.jsonPrimitive?.content
    val colorHex = widget.options?.get("color")?.jsonPrimitive?.content
    
    val iconColor = remember(colorHex) {
        parseColorSafely(colorHex) ?: Color(0xFF4F46E5) // Default Snapp Blue
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = widget.title ?: "",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            if (!iconName.isNullOrBlank()) {
                Icon(
                    imageVector = iconForName(iconName),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun parseColorSafely(colorStr: String?): Color? {
    if (colorStr.isNullOrBlank()) return null
    return try {
        val color = when (colorStr.lowercase()) {
            "green" -> "#4CAF50"
            "red" -> "#F44336"
            "blue" -> "#2196F3"
            "yellow" -> "#FFEB3B"
            "purple" -> "#9C27B0"
            "orange" -> "#FF9800"
            else -> colorStr
        }
        val formattedColor = if (color.startsWith("#")) color else "#$color"
        Color(android.graphics.Color.parseColor(formattedColor))
    } catch (e: Exception) {
        null
    }
}

private fun extractMetricValue(data: JsonElement?, widget: PageWidget): String {
    if (data == null) return "-"

    val matchKey = normalizeKey(widget.dataKey)
    val matchTitle = normalizeKey(widget.title)
    val matchGroup = normalizeKey(widget.options?.get("group")?.jsonPrimitive?.content)

    return when (data) {
        is JsonObject -> {
            // 1. Try aggregates
            val aggregates = data["aggregates"] as? JsonArray
            val aggregateValue = aggregates?.firstNotNullOfOrNull { item ->
                val obj = item as? JsonObject ?: return@firstNotNullOfOrNull null
                val name = normalizeKey(obj["name"]?.jsonPrimitiveOrString())
                if (name.isNotEmpty() && (name == matchKey || name == matchTitle || name == matchGroup || 
                    (matchGroup.isNotEmpty() && name.contains(matchGroup)) ||
                    (matchKey.isNotEmpty() && name.contains(matchKey)))) {
                    obj["value"]?.jsonPrimitiveOrString()
                } else null
            }
            if (aggregateValue != null) return aggregateValue

            // 2. Try records
            val records = (data["records"] as? JsonArray) ?: (data["data"] as? JsonArray)
            val recordValue = records?.firstNotNullOfOrNull { item ->
                val obj = item as? JsonObject ?: return@firstNotNullOfOrNull null
                val name = normalizeKey(obj["name"]?.jsonPrimitiveOrString())
                if (name.isNotEmpty() && (name == matchKey || name == matchTitle || name == matchGroup)) {
                    obj["value"]?.jsonPrimitiveOrString() ?: obj["total"]?.jsonPrimitiveOrString()
                } else null
            } ?: records?.firstOrNull()?.let { item ->
                val obj = item as? JsonObject
                obj?.get("value")?.jsonPrimitiveOrString()
                    ?: obj?.get("total")?.jsonPrimitiveOrString()
                    ?: obj?.firstNumericValue()
            }
            if (recordValue != null) return recordValue

            // 3. Direct fields
            data["value"]?.jsonPrimitiveOrString()
                ?: data["total"]?.jsonPrimitiveOrString()
                ?: (data["data"] as? JsonPrimitive)?.content
                ?: "-"
        }
        is JsonArray -> {
            val matchValue = data.firstNotNullOfOrNull { item ->
                val obj = item as? JsonObject ?: return@firstNotNullOfOrNull null
                val name = normalizeKey(obj["name"]?.jsonPrimitiveOrString())
                if (name.isNotEmpty() && (name == matchKey || name == matchTitle || name == matchGroup)) {
                    obj["value"]?.jsonPrimitiveOrString() ?: obj["total"]?.jsonPrimitiveOrString()
                } else null
            }
            matchValue
                ?: (data.firstOrNull() as? JsonObject)?.get("value")?.jsonPrimitiveOrString()
                ?: (data.firstOrNull() as? JsonObject)?.get("total")?.jsonPrimitiveOrString()
                ?: (data.firstOrNull() as? JsonObject)?.firstNumericValue()
                ?: "-"
        }
        is JsonPrimitive -> data.content
        else -> "-"
    }
}

private fun JsonObject.firstNumericValue(): String? {
    return entries.firstNotNullOfOrNull { (_, value) ->
        val content = (value as? JsonPrimitive)?.content
        if (content?.toDoubleOrNull() != null) content else null
    }
}

private fun normalizeKey(value: String?): String {
    return value
        ?.trim()
        ?.lowercase()
        ?.replace(" ", "_")
        ?.replace(Regex("[^a-z0-9_]+"), "")
        .orEmpty()
}

private fun JsonElement.jsonPrimitiveOrString(): String {
    return when (this) {
        is JsonPrimitive -> content
        else -> this.toString()
    }
}
