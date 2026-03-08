package com.snapp.android.ui.widget

/** Sealed: LoadPage | OpenRecord | CRUD. See ARCHITECTURE ui/widget/. */
sealed class WidgetAction {
    data class LoadPage(val dataKey: String, val page: Int) : WidgetAction()
    data class OpenRecord(val id: String) : WidgetAction()
}
