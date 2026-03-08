package com.snapp.android.ui.widget

import com.snapp.data.model.page.PageWidget
import kotlinx.serialization.json.JsonObject

/** Context passed to widget renderers. See ARCHITECTURE ui/widget/. */
data class WidgetRenderContext(
    val widget: PageWidget,
    val widgetData: Any? = null,
    val allWidgetData: Map<String, JsonObject> = emptyMap(),
    val onAction: (WidgetAction) -> Unit = {}
)
