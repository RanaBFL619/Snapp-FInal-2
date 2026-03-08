package com.snapp.android.ui.widget

import android.util.Log
import androidx.compose.runtime.Composable
import com.snapp.android.ui.scaffold.skeleton.WidgetSkeleton
import com.snapp.data.model.page.PageWidget
import kotlinx.serialization.json.JsonObject

/** Recursive widget renderer. See ARCHITECTURE ui/widget/. */
@Composable
fun WidgetHost(
    widget: PageWidget,
    widgetData: Any? = null,
    allWidgetData: Map<String, JsonObject> = emptyMap(),
    isLoading: Boolean = false,
    onAction: (WidgetAction) -> Unit = {}
) {
    val renderer = WidgetRegistry.get(widget.type) ?: return WidgetFallback(widget)
    
    // If widgetData is null, try to find it in the global map by widget ID
    val finalData = widgetData ?: allWidgetData[widget.id]
    
    if (finalData == null) {
        Log.v("WidgetHost", "No data for widget ${widget.id} (type=${widget.type}). allWidgetData keys: ${allWidgetData.keys}")
    } else {
        Log.v("WidgetHost", "Found data for widget ${widget.id}")
    }
    
    val context = WidgetRenderContext(widget, finalData, allWidgetData, onAction)

    if (isLoading && finalData == null) {
        WidgetSkeleton()
        return
    }

    renderer.invoke(context)
}
