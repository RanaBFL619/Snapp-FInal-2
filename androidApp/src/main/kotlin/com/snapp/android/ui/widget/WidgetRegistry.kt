package com.snapp.android.ui.widget

import androidx.compose.runtime.Composable
import com.snapp.android.ui.widget.renderer.BarChartWidget
import com.snapp.android.ui.widget.renderer.CardWidget
import com.snapp.android.ui.widget.renderer.FieldWidget
import com.snapp.android.ui.widget.renderer.ImageWidget
import com.snapp.android.ui.widget.renderer.LineChartWidget
import com.snapp.android.ui.widget.renderer.MetricWidget
import com.snapp.android.ui.widget.renderer.PieChartWidget
import com.snapp.android.ui.widget.renderer.TableWidget

/** Registry: type String → @Composable renderer. See ARCHITECTURE ui/widget/. */
object WidgetRegistry {
    private val builders = mutableMapOf<String, @Composable (WidgetRenderContext) -> Unit>()

    fun register(type: String, builder: @Composable (WidgetRenderContext) -> Unit) {
        builders[type] = builder
    }

    fun get(type: String): (@Composable (WidgetRenderContext) -> Unit)? = builders[type]
}

fun registerWidgets() {
    val registry = WidgetRegistry // import

    // Card
    registry.register("card") { CardWidget(it) }
    registry.register("record") { CardWidget(it) }

    // Simple widgets
    registry.register("field") { FieldWidget(it) }
    registry.register("image") { ImageWidget(it) }
    registry.register("metric") { MetricWidget(it) }

    // Charts
    registry.register("bar") { BarChartWidget(it) }
    registry.register("bar-chart") { BarChartWidget(it) }
    registry.register("pie") { PieChartWidget(it) }
    registry.register("pie-chart") { PieChartWidget(it) }
    registry.register("donut") { PieChartWidget(it) }
    registry.register("donut-chart") { PieChartWidget(it) }
    registry.register("line") { LineChartWidget(it) }
    registry.register("line-chart") { LineChartWidget(it) }
    registry.register("area") { LineChartWidget(it) }
    registry.register("area-chart") { LineChartWidget(it) }

    // Table/List
    registry.register("table") { TableWidget(it) }
    registry.register("list") { TableWidget(it) }
}
