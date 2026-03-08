package com.snapp.android.ui.widget.renderer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapp.android.ui.scaffold.skeleton.WidgetSkeleton
import com.snapp.android.ui.widget.WidgetRenderContext
import com.snapp.android.ui.widget.WidgetAction
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun TableWidget(context: WidgetRenderContext) {
    val widget = context.widget
    val data = context.widgetData as? JsonObject

    // Handle wrapping: { data: { ... } } or { result: { ... } }
    val unwrappedData = data?.get("data")?.jsonObject
        ?: data?.get("result")?.jsonObject
        ?: data

    val records = unwrappedData?.get("records")?.jsonArray

    // Show skeleton if data is missing and we don't have records yet
    if (records == null) {
        WidgetSkeleton()
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Widget Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = widget.title ?: widget.type,
                    style = MaterialTheme.typography.titleMedium
                )

                // Refresh button
                TextButton(onClick = { context.onAction(WidgetAction.LoadPage(widget.dataKey ?: "", 1)) }) {
                    Text("Refresh")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (!records.isNullOrEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(records.size) { index ->
                        val record = records[index].jsonObject

                        TableRow(
                            record = record!!,
                            onClick = {
                                // Get record ID and navigate
                                val recordId = record["id"]?.jsonPrimitive?.content
                                    ?: record["recordId"]?.jsonPrimitive?.content
                                if (recordId != null) {
                                    context.onAction(WidgetAction.OpenRecord(recordId))
                                }
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
private fun TableRow(
    record: JsonObject,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            record.entries.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value.jsonPrimitive?.content ?: value.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
// Extension properties for JsonElement
private val JsonElement.jsonArray: JsonArray?
    get() = this as? JsonArray
private val JsonElement.jsonObject: JsonObject?
    get() = this as? JsonObject
private val JsonElement.jsonPrimitive: JsonPrimitive?
    get() = this as? JsonPrimitive