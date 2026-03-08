package com.snapp.domain.usecase.page

import com.snapp.data.model.page.PageWidget

class CollectWidgetDataKeysUseCase {

    operator fun invoke(widgets: List<PageWidget>): List<Pair<PageWidget, Boolean>> {
        val result = mutableListOf<Pair<PageWidget, Boolean>>()
        collectRecursive(widgets, result)
        return result
    }

    private fun collectRecursive(
        widgets: List<PageWidget>,
        out: MutableList<Pair<PageWidget, Boolean>>
    ) {
        for (widget in widgets) {
            val dataKey = widget.dataKey
            if (!dataKey.isNullOrBlank()) {
                val isTable = widget.type in setOf("table", "list", "comprehensiveTable")
                out.add(widget to isTable)
            }
            widget.components?.let { collectRecursive(it, out) }
        }
    }
}
