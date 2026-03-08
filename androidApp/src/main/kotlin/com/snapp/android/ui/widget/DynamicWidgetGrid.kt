package com.snapp.android.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapp.data.model.page.PageConfig
import com.snapp.data.model.page.PageWidget
import kotlinx.serialization.json.JsonObject


@Composable
fun DynamicWidgetGrid(
    page: PageConfig?,
    widgetData: Map<String, JsonObject>,
    loadingWidgets: Set<String> = emptySet(),
    onRefreshWidget: (PageWidget) -> Unit,
    onNavigateRoute: (String) -> Unit,
    onCreateRecord: (PageWidget, Map<String, String>) -> Unit,
    onUpdateRecord: (PageWidget, String, Map<String, String>) -> Unit,
    onDeleteRecord: (PageWidget, String) -> Unit,
    onLoadTableData: (PageWidget, Int, Int, String?, String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (page == null) return

    val sortedWidgets = page.components.sortedWith(
        compareBy<PageWidget> { it.gridRow ?: 0 }.thenBy { it.gridColumn ?: 0 }
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(page.layout?.columns ?: 2),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        items(sortedWidgets, span = { widget ->
            GridItemSpan(widget.width ?: 1)
        }) { widget ->
            val isLoading = loadingWidgets.contains(widget.id)
            val data = widgetData[widget.id]

            WidgetCard(
                widget = widget,
                isLoading = isLoading,
                data = data,
                allWidgetData = widgetData,
                onRefresh = { onRefreshWidget(widget) },
                onNavigateRoute = onNavigateRoute
            )
        }
    }
}

@Composable
private fun WidgetCard(
    widget: PageWidget,
    isLoading: Boolean,
    data: JsonObject?,
    allWidgetData: Map<String, JsonObject>,
    onRefresh: () -> Unit,
    onNavigateRoute: (String) -> Unit
) {
    // If it's a card/layout component, it might handle its own Card UI, 
    // but for simple widgets we wrap them in a Card here.
    val isLayoutComponent = widget.type in setOf("card", "record", "container")
    
    if (isLayoutComponent) {
        WidgetHost(
            widget = widget,
            widgetData = data,
            allWidgetData = allWidgetData,
            isLoading = isLoading,
            onAction = { /* handle action */ }
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!widget.title.isNullOrBlank()) {
                    Text(
                        text = widget.title!!,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                WidgetHost(
                    widget = widget,
                    widgetData = data,
                    allWidgetData = allWidgetData,
                    isLoading = isLoading,
                    onAction = { /* handle action */ }
                )
            }
        }
    }
}
