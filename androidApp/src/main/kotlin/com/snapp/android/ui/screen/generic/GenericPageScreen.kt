package com.snapp.android.ui.screen.generic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapp.android.ui.scaffold.skeleton.DashboardSkeleton
import com.snapp.android.ui.widget.DynamicWidgetGrid
import com.snapp.android.viewmodel.GenericPageViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun GenericPageScreen(
    slug: String,
    viewModel: GenericPageViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Force load whenever the slug changes and doesn't match the current active slug
    LaunchedEffect(slug) {
        if (uiState.activeSlug != slug) {
            viewModel.loadPage(slug)
        }
    }

    when {
        uiState.isInitialLoading -> {
            // Show skeleton while loading a new page configuration
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
                Button(onClick = { viewModel.loadPage(slug) }) {
                    Text("Retry")
                }
            }
        }

        uiState.page != null -> {
            DynamicWidgetGrid(
                page = uiState.page!!,
                widgetData = uiState.widgetData,
                loadingWidgets = uiState.loadingWidgets,
                onRefreshWidget = viewModel::refreshWidget,
                onNavigateRoute = { route ->
                    // Navigation logic
                },
                onCreateRecord = { widget, values ->
                    viewModel.createRecord(widget, values)
                },
                onUpdateRecord = { widget, id, values ->
                    viewModel.updateRecord(widget, id, values)
                },
                onDeleteRecord = { widget, id ->
                    viewModel.deleteRecord(widget, id)
                },
                onLoadTableData = { widget, page, size, sort, dir, filters ->
                    viewModel.loadTableData(widget, page, size, sort, dir, filters)
                }
            )
        }
    }
}
