package com.snapp.android.ui.widget.renderer


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.snapp.android.ui.widget.WidgetRenderContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun FieldWidget(context: WidgetRenderContext) {
    val widget = context.widget
    val data = context.widgetData as? JsonObject

    val fieldValue = extractFieldValue(data, widget.options)

    Text(
        text = fieldValue,
        style = MaterialTheme.typography.bodyLarge
    )
}
private fun extractFieldValue(data: JsonObject?, options: kotlinx.serialization.json.JsonObject?): String {
    if (data == null) return "-"

    // Try to get key from options
    val key = options?.get("key")?.let { (it as? JsonPrimitive)?.content }

    return if (key != null) {
        data[key]?.jsonPrimitiveOrString() ?: "-"
    } else {
        // Fallback: show first value
        data.entries.firstOrNull()?.value?.jsonPrimitiveOrString() ?: "-"
    }
}
private fun kotlinx.serialization.json.JsonElement.jsonPrimitiveOrString(): String {
    return when (this) {
        is JsonPrimitive -> content
        else -> this.toString()
    }
}