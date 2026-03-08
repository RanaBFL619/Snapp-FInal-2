package com.snapp.android.ui.widget.renderer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapp.android.ui.widget.WidgetRenderContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun LineChartWidget(context: WidgetRenderContext) {
    val widget = context.widget
    val data = context.widgetData as? JsonObject

    val chartData = parseLineChartData(data)

    if (chartData.values.isEmpty()) {
        Text("No chart data", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val max = chartData.values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val labels = chartData.labels
    val values = chartData.values
    val lineColor = chartData.colors.firstOrNull() ?: Color(0xFF4F46E5)

    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 28f
        textAlign = android.graphics.Paint.Align.CENTER
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(220.dp).padding(8.dp)) {
        val paddingLeft = 80f
        val paddingBottom = 50f
        val chartWidth = size.width - paddingLeft
        val chartHeight = size.height - paddingBottom
        
        if (values.isEmpty()) return@Canvas

        val stepX = if (values.size == 1) chartWidth / 2 else chartWidth / (values.size - 1)
        val axisColor = Color.Gray

        // Draw axes
        drawLine(color = axisColor, start = Offset(paddingLeft, 0f), end = Offset(paddingLeft, chartHeight), strokeWidth = 2f)
        drawLine(color = axisColor, start = Offset(paddingLeft, chartHeight), end = Offset(size.width, chartHeight), strokeWidth = 2f)

        // Y-axis Ticks and Labels
        val tickCount = 5
        repeat(tickCount + 1) { i ->
            val ratio = i / tickCount.toFloat()
            val y = chartHeight - ratio * chartHeight
            val value = (ratio * max).toInt()
            
            drawLine(color = axisColor, start = Offset(paddingLeft - 10, y), end = Offset(paddingLeft, y), strokeWidth = 2f)
            
            // Adjust label position to be more centered vertically with the tick
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(), 
                paddingLeft - 45f, 
                y + 10f, 
                textPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
            )
        }

        // Line Path
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) paddingLeft + chartWidth / 2 else paddingLeft + index * stepX
            val y = chartHeight - (value / max) * chartHeight
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 5f, cap = StrokeCap.Round))

        // Points
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) paddingLeft + chartWidth / 2 else paddingLeft + index * stepX
            val y = chartHeight - (value / max) * chartHeight
            drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
        }

        // X-axis Labels
        labels.forEachIndexed { index, label ->
            if (index < values.size) {
                val x = if (values.size == 1) paddingLeft + chartWidth / 2 else paddingLeft + index * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    label, 
                    x, 
                    size.height - 10f, 
                    textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER }
                )
            }
        }
    }
}

data class LineChartData(
    val labels: List<String> = emptyList(),
    val values: List<Float> = emptyList(),
    val colors: List<Color> = emptyList()
)

private fun parseLineChartData(data: JsonObject?): LineChartData {
    if (data == null) return LineChartData()

    val unwrappedData = data["data"]?.let { if (it is JsonObject) it else null }
        ?: data["result"]?.let { if (it is JsonObject) it else null }
        ?: data

    val defaultColors = listOf(Color(0xFF4F46E5), Color(0xFF0EA5E9), Color(0xFF10B981))

    val directValues = (unwrappedData["values"] as? JsonArray)
        ?.mapNotNull { it.jsonPrimitiveOrFloat() }
        .orEmpty()

    if (directValues.isNotEmpty()) {
        val labels = (unwrappedData["labels"] as? JsonArray)
            ?.map { it.jsonPrimitiveOrString() }
            .orEmpty()
            .ifEmpty { directValues.indices.map { "Item ${it + 1}" } }

        return LineChartData(
            labels = labels,
            values = directValues,
            colors = defaultColors
        )
    }

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
                    val label = groupObj["group"]?.jsonPrimitiveOrString() ?: "Item"
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
            return LineChartData(labels = labels, values = chartValues, colors = defaultColors)
        }
    }

    val records = unwrappedData["records"] as? JsonArray
    if (records != null) {
        val labels = mutableListOf<String>()
        val chartValues = mutableListOf<Float>()

        records.forEachIndexed { index, item ->
            val obj = item as? JsonObject ?: return@forEachIndexed
            val label = obj["name"]?.jsonPrimitiveOrString()
                ?: obj["label"]?.jsonPrimitiveOrString()
                ?: obj["date"]?.jsonPrimitiveOrString()
                ?: obj["orderDate"]?.jsonPrimitiveOrString()
                ?: "Item ${index + 1}"
            val value = obj["value"]?.jsonPrimitiveOrFloat()
                ?: obj["total"]?.jsonPrimitiveOrFloat()
                ?: obj["count"]?.jsonPrimitiveOrFloat()

            if (value != null) {
                labels.add(label)
                chartValues.add(value)
            }
        }

        return LineChartData(labels = labels, values = chartValues, colors = defaultColors)
    }

    return LineChartData()
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
