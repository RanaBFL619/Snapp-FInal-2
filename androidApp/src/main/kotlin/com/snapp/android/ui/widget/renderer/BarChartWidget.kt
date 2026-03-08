package com.snapp.android.ui.widget.renderer


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snapp.android.ui.widget.WidgetRenderContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun BarChartWidget(context: WidgetRenderContext) {
    val widget = context.widget
    val data = context.widgetData as? JsonObject

    val chartData = parseBarChartData(data)

    if (chartData.labels.isEmpty() || chartData.values.isEmpty()) {
        Text("No chart data", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val maxValue = chartData.values.maxOrNull()?.takeIf { it > 0f } ?: 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val barCount = chartData.labels.size
            val spacing = 16f
            val totalSpacing = spacing * (barCount + 1)
            val barWidth = (size.width - totalSpacing) / barCount

            chartData.values.forEachIndexed { index, value ->
                val barHeight = (value / maxValue) * size.height * 0.85f
                val x = spacing + index * (barWidth + spacing)
                val y = size.height - barHeight

                val color = chartData.colors.getOrElse(index) {
                    val defaultColors = listOf(
                        Color(0xFF4F46E5), Color(0xFF0EA5E9), Color(0xFF10B981),
                        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)
                    )
                    defaultColors[index % defaultColors.size]
                }
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        if (chartData.labels.isNotEmpty()) {
            Text(
                text = chartData.labels.filter { it.isNotBlank() }.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

data class BarChartData(
    val labels: List<String> = emptyList(),
    val values: List<Float> = emptyList(),
    val colors: List<Color> = emptyList()
)

private fun parseBarChartData(data: JsonObject?): BarChartData {
    if (data == null) return BarChartData()

    // Handle wrapping: { data: { ... } } or { result: { ... } }
    val unwrappedData = data["data"]?.let { if (it is JsonObject) it else null }
        ?: data["result"]?.let { if (it is JsonObject) it else null }
        ?: data

    val defaultColors = listOf(
        Color(0xFF4F46E5), Color(0xFF0EA5E9), Color(0xFF10B981),
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)
    )

    // Try direct values
    val directValues = (unwrappedData["values"] as? JsonArray)
        ?.mapNotNull { it.jsonPrimitiveOrFloat() }
        .orEmpty()

    if (directValues.isNotEmpty()) {
        val labels = (unwrappedData["labels"] as? JsonArray)
            ?.map { it.jsonPrimitiveOrString() }
            .orEmpty()
            .ifEmpty { directValues.indices.map { "Item ${it + 1}" } }

        return BarChartData(
            labels = labels,
            values = directValues,
            colors = defaultColors
        )
    }

    // Try aggregates (often used for charts from views)
    val aggregates = unwrappedData["aggregates"] as? JsonArray
    if (aggregates != null) {
        val labels = mutableListOf<String>()
        val chartValues = mutableListOf<Float>()

        aggregates.forEach { item ->
            val obj = item as? JsonObject ?: return@forEach
            val groups = obj["groups"] as? JsonArray
            if (groups != null) {
                groups.forEach { groupItem ->
                    val groupObj = groupItem as? JsonObject ?: return@forEach
                    val label = groupObj["group"]?.jsonPrimitiveOrString()?.ifBlank { "Other" } ?: "Other"
                    val value = groupObj["value"]?.jsonPrimitiveOrFloat()
                    if (value != null) {
                        labels.add(label)
                        chartValues.add(value)
                    }
                }
            } else {
                val label = obj["name"]?.jsonPrimitiveOrString() ?: "Item"
                val value = obj["value"]?.jsonPrimitiveOrFloat()
                if (value != null) {
                    labels.add(label)
                    chartValues.add(value)
                }
            }
        }
        if (chartValues.isNotEmpty()) {
            return BarChartData(labels = labels, values = chartValues, colors = defaultColors)
        }
    }

    // Try records
    val records = unwrappedData["records"] as? JsonArray
    if (records != null) {
        val labels = mutableListOf<String>()
        val values = mutableListOf<Float>()

        records.forEachIndexed { index, item ->
            val obj = item as? JsonObject ?: return@forEachIndexed
            val label = obj["name"]?.jsonPrimitiveOrString()
                ?: obj["label"]?.jsonPrimitiveOrString()
                ?: obj["category"]?.jsonPrimitiveOrString()
                ?: "Item ${index + 1}"
            val value = obj["value"]?.jsonPrimitiveOrFloat()
                ?: obj["total"]?.jsonPrimitiveOrFloat()

            if (value != null) {
                labels.add(label)
                values.add(value)
            }
        }

        return BarChartData(labels = labels, values = values, colors = defaultColors)
    }

    return BarChartData()
}

private fun kotlinx.serialization.json.JsonElement.jsonPrimitiveOrFloat(): Float? {
    return (this as? JsonPrimitive)?.content?.toFloatOrNull()
}

private fun kotlinx.serialization.json.JsonElement.jsonPrimitiveOrString(): String {
    return when (this) {
        is JsonPrimitive -> content
        else -> ""
    }
}
