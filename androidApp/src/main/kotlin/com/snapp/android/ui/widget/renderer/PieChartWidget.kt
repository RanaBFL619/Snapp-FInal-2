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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.snapp.android.ui.widget.WidgetRenderContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun PieChartWidget(context: WidgetRenderContext) {
    val widget = context.widget
    val data = context.widgetData as? JsonObject

    val chartData = parsePieChartData(data)

    if (chartData.values.isEmpty()) {
        Text("No chart data", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val total = chartData.values.sum().takeIf { it > 0f } ?: 1f
    val isDonut = widget.type == "donut-chart" || widget.type == "donut"

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            var startAngle = -90f
            val chartSize = size.minDimension - 40f
            val radius = chartSize / 2

            chartData.values.forEachIndexed { index, value ->
                val sweepAngle = (value / total) * 360f
                val color = chartData.colors.getOrElse(index) {
                    val defaultColors = listOf(
                        Color(0xFF4F46E5), Color(0xFF0EA5E9), Color(0xFF10B981),
                        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)
                    )
                    defaultColors[index % defaultColors.size]
                }

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = !isDonut,
                    topLeft = Offset((size.width - chartSize) / 2, (size.height - chartSize) / 2),
                    size = Size(chartSize, chartSize),
                    style = if (isDonut) Stroke(width = 40f) else Fill
                )

                startAngle += sweepAngle
            }
        }

        if (chartData.labels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                chartData.labels.filter { it.isNotBlank() }.take(4).forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

data class PieChartData(
    val labels: List<String> = emptyList(),
    val values: List<Float> = emptyList(),
    val colors: List<Color> = emptyList()
)

private fun parsePieChartData(data: JsonObject?): PieChartData {
    if (data == null) return PieChartData()

    // Handle wrapping: { data: { ... } } or { result: { ... } }
    val unwrappedData = data["data"]?.let { if (it is JsonObject) it else null }
        ?: data["result"]?.let { if (it is JsonObject) it else null }
        ?: data

    val defaultColors = listOf(
        Color(0xFF4F46E5), Color(0xFF0EA5E9), Color(0xFF10B981),
        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)
    )

    // Try direct values
    val values = (unwrappedData["values"] as? JsonArray)
        ?.mapNotNull { it.jsonPrimitiveOrFloat() }
        .orEmpty()

    if (values.isNotEmpty()) {
        val labels = (unwrappedData["labels"] as? JsonArray)
            ?.map { it.jsonPrimitiveOrString() }
            .orEmpty()
            .ifEmpty { values.indices.map { "Item ${it + 1}" } }

        return PieChartData(
            labels = labels,
            values = values,
            colors = defaultColors
        )
    }

    // Try aggregates (common for pie charts from views)
    val aggregates = unwrappedData["aggregates"] as? JsonArray
    if (aggregates != null) {
        val labels = mutableListOf<String>()
        val chartValues = mutableListOf<Float>()

        aggregates.forEach { item ->
            val obj = item as? JsonObject ?: return@forEach
            // Handle group-based aggregates: { name: "...", groups: [{ group: "A", value: 10 }, ...] }
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
            return PieChartData(labels = labels, values = chartValues, colors = defaultColors)
        }
    }

    // Try records
    val records = unwrappedData["records"] as? JsonArray
    if (records != null) {
        val labels = mutableListOf<String>()
        val chartValues = mutableListOf<Float>()

        records.forEachIndexed { index, item ->
            val obj = item as? JsonObject ?: return@forEachIndexed
            val label = obj["name"]?.jsonPrimitiveOrString()
                ?: obj["label"]?.jsonPrimitiveOrString()
                ?: "Item ${index + 1}"
            val value = obj["value"]?.jsonPrimitiveOrFloat()
                ?: obj["total"]?.jsonPrimitiveOrFloat()

            if (value != null) {
                labels.add(label)
                chartValues.add(value)
            }
        }

        return PieChartData(labels = labels, values = chartValues, colors = defaultColors)
    }

    return PieChartData()
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
