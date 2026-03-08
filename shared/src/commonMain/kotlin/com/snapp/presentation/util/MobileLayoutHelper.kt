package com.snapp.presentation.util

import com.snapp.data.model.page.PageWidget

/**
 * Shared mobile layout algorithm: converts desktop grid widget list into positioned items.
 * Widgets wider than half the grid become full-width; others use half width (side-by-side).
 * Order follows server gridRow / gridColumn (1-indexed). Reused by iOS and Android (KMP).
 */
object MobileLayoutHelper {

    private const val MAX_ROWS = 200

    fun generateMobileLayout(widgets: List<PageWidget>, gridColumns: Int): List<PositionedWidget> {
        if (gridColumns <= 0) return emptyList()

        val grid = mutableListOf<BooleanArray>()
        val result = mutableListOf<PositionedWidget>()

        val sorted = widgets.sortedWith(
            compareBy<PageWidget> { it.gridRow ?: 0 }.thenBy { it.gridColumn ?: 0 }
        )

        for (widget in sorted) {
            val rawWidth = widget.width ?: 0
            val height = maxOf(1, widget.height ?: 1)
            val mobileWidth = if (rawWidth > gridColumns / 2) gridColumns else gridColumns / 2

            findPosition(grid, gridColumns, mobileWidth, height)?.let { (row, col) ->
                markGrid(grid, row, col, mobileWidth, height, gridColumns)
                result.add(
                    PositionedWidget(
                        widget = widget,
                        x = col,
                        y = row,
                        w = mobileWidth,
                        h = height
                    )
                )
            }
        }

        return result
    }

    private fun ensureRows(grid: MutableList<BooleanArray>, upTo: Int, cols: Int) {
        while (grid.size < upTo) {
            grid.add(BooleanArray(cols))
        }
    }

    private fun findPosition(
        grid: MutableList<BooleanArray>,
        gridColumns: Int,
        w: Int,
        h: Int
    ): Pair<Int, Int>? {
        val colStep = maxOf(1, gridColumns / 2)
        var row = 0
        while (row <= MAX_ROWS) {
            ensureRows(grid, row + h, gridColumns)
            var col = 0
            while (col + w <= gridColumns) {
                if (canFit(grid, row, col, w, h)) return row to col
                col += colStep
            }
            row++
        }
        return null
    }

    private fun canFit(grid: List<BooleanArray>, row: Int, col: Int, w: Int, h: Int): Boolean {
        for (r in row until (row + h)) {
            if (r >= grid.size) return true
            val line = grid[r]
            for (c in col until (col + w)) {
                if (c < line.size && line[c]) return false
            }
        }
        return true
    }

    private fun markGrid(
        grid: MutableList<BooleanArray>,
        row: Int,
        col: Int,
        w: Int,
        h: Int,
        gridColumns: Int
    ) {
        ensureRows(grid, row + h, gridColumns)
        for (r in row until (row + h)) {
            for (c in col until (col + w)) {
                if (c < grid[r].size) grid[r][c] = true
            }
        }
    }
}
