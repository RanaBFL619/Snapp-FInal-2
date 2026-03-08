package com.snapp.android.ui.widget.renderer


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.snapp.android.ui.widget.WidgetRenderContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


@Composable
fun ImageWidget(context: WidgetRenderContext) {
    val data = context.widgetData as? JsonObject

    val url = extractImageUrl(data)

    if (url.isNullOrBlank()) {
        Text(
            text = "No image",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentScale = ContentScale.Crop
    )
}
private fun extractImageUrl(data: JsonObject?): String? {
    if (data == null) return null

    return data["url"]?.jsonPrimitiveOrString()
        ?: data["image"]?.jsonPrimitiveOrString()
        ?: data["src"]?.jsonPrimitiveOrString()
        ?: data["records"]?.jsonPrimitiveOrString()
}
private fun kotlinx.serialization.json.JsonElement.jsonPrimitiveOrString(): String {
    return when (this) {
        is JsonPrimitive -> content
        else -> this.toString()
    }
}
