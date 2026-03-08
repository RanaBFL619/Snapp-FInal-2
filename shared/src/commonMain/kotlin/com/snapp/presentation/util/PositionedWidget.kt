package com.snapp.presentation.util

import com.snapp.data.model.page.PageWidget

/**
 * Result of mobile layout: a widget placed at (x, y) with width w and height h.
 * Used by both iOS and Android so layout logic lives in shared code (KMP best practice).
 */
data class PositionedWidget(
    val widget: PageWidget,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int
)
