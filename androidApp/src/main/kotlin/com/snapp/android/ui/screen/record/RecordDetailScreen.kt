package com.snapp.android.ui.screen.record

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapp.android.ui.scaffold.skeleton.DashboardSkeleton
import com.snapp.android.ui.widget.DynamicWidgetGrid
import com.snapp.android.viewmodel.RecordViewModel
import com.snapp.android.viewmodel.RecordUiState
import com.snapp.data.model.page.PageConfig
import com.snapp.data.model.page.PageWidget
import org.koin.androidx.compose.koinViewModel
@Composable
fun RecordDetailScreen(
    recordId: String,
    viewModel: RecordViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    when {
        uiState.isInitialLoading -> {
            DashboardSkeleton()
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.errorMessage ?: "Failed to load",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.loadRecord(recordId) }) {
                    Text("Retry")
                }
            }
        }

        uiState.recordData != null -> {
            // Convert record data to PageConfig for DynamicWidgetGrid
            val pageConfig = convertRecordToPageConfig(uiState!!)

            DynamicWidgetGrid(
                page = pageConfig,
                widgetData = uiState.widgetData,
                loadingWidgets = uiState.loadingWidgets,
                onRefreshWidget = { widget -> viewModel.refreshWidget(widget) },
                onNavigateRoute = { route ->
                    // Handle navigation
                },
                onCreateRecord = { widget, values ->
                    // Extract schema from widget and call createRecord
                },
                onUpdateRecord = { widget, id, values ->
                    // Extract schema from widget and call updateRecord
                },
                onDeleteRecord = { widget, id ->
                    // Extract schema from widget and call deleteRecord
                },
                onLoadTableData = { widget, page, size, sort, dir, filters ->
                    viewModel.loadTableData(widget, page, size, sort, dir, filters)
                }
            )
        }
    }
}
private fun convertRecordToPageConfig(uiState: RecordUiState): PageConfig {
    val recordData = uiState.recordData ?: return PageConfig(title = "Record")

    // Convert record fields to widgets
    val widgets = recordData.map { (key, value) ->
        PageWidget(
            id = key,
            type = "field",
            title = key.replaceFirstChar { it.uppercase() },
            dataKey = key
        )
    }

    return PageConfig(
        title = "Record Details",
        components = widgets
    )
}
