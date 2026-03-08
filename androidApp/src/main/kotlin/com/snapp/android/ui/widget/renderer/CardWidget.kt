package com.snapp.android.ui.widget.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.snapp.android.ui.widget.WidgetHost
import com.snapp.android.ui.widget.WidgetRenderContext


@Composable
fun CardWidget(context: WidgetRenderContext) {
    val widget = context.widget
    val data = context.widgetData
    val allData = context.allWidgetData

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title + Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = widget.title ?: widget.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!widget.desc.isNullOrBlank()) {
                        Text(
                            text = widget.desc!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Render nested components or fallback
            val components = widget.components
            if (!components.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    components.forEach { nestedWidget ->
                        // Try to get data from current object OR from the global map
                        val nestedData = (data as? kotlinx.serialization.json.JsonObject)?.get(nestedWidget.id)
                            ?: (data as? kotlinx.serialization.json.JsonObject)?.get(nestedWidget.dataKey ?: "")
                        
                        WidgetHost(
                            widget = nestedWidget,
                            widgetData = nestedData,
                            allWidgetData = allData,
                            onAction = context.onAction
                        )
                    }
                }
            } else {
                // Fallback: show raw data if no components
                if (data != null) {
                    Text(
                        text = data.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}