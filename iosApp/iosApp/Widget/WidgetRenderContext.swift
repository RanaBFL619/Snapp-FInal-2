import SwiftUI
import shared

/// Context passed to every widget renderer — same as web’s single `widget` prop (PageLayoutWidget) plus callbacks.
/// Web: GridItemContainer passes widget={ ...component, detailsFormData, handleInputChange, resetDetailsFormData } to GridItem → WidgetRenderer → Component(widget). We pass WidgetRenderContext so each view receives the same data.
///
/// Field mapping (widgetRegistry / WidgetRenderer):
/// - widget         → widget (id, type, title, dataKey, options, components, gridRow, gridColumn, width, height, actions, etc.)
/// - widgetData     → pre-fetched data for this widget (allWidgetData[widget.dataKey])
/// - allWidgetData  → used by container widgets to pass child data by dataKey
/// - detailsFormData → widget.detailsFormData (injected by page)
/// - detailsData     → widget.detailsData (injected on record detail; nil on generic page)
/// - onInputChange   → widget.handleInputChange
/// - onResetDetailsFormData → widget.resetDetailsFormData
/// - onAction        → navigation/CRUD (e.g. openRecord)
struct WidgetRenderContext {
    // ---- From API (page config) ----
    /// This widget's config: id, type, title, dataKey, options, components, gridRow, gridColumn, width, height, actions, etc.
    let widget: PageWidget
    /// This widget's fetched data (raw JSON string), keyed by widget.dataKey from allWidgetData.
    let widgetData: String?
    /// Full page-level widget data map [dataKey → JSON string] for container widgets to pass to children.
    let allWidgetData: [String: String]

    // ---- Injected by page (same as web GenericPage / details page) ----
    /// Page-level form state. Web: widget.detailsFormData.
    let detailsFormData: [String: Any]
    /// Record context when viewing a record. Web: widget.detailsData. Nil on generic page.
    let detailsData: [String: Any]?
    /// Field value change. Web: widget.handleInputChange(componentId, key, value).
    let onInputChange: (String, String, Any) -> Void
    /// Clear form state. Web: widget.resetDetailsFormData().
    let onResetDetailsFormData: () -> Void

    // ---- Actions (navigation, CRUD) ----
    let onAction: (WidgetAction) -> Void
}
