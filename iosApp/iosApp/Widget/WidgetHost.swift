import SwiftUI
import shared

/// Renders one widget. Wraps it in WidgetErrorWrapper so only this widget shows error on failure.
struct WidgetHost: View {
    let widget: PageWidget
    let allWidgetData: [String: String]
    let detailsFormData: [String: Any]
    let detailsData: [String: Any]?
    let onInputChange: (String, String, Any) -> Void
    let onResetDetailsFormData: () -> Void
    let onAction: (WidgetAction) -> Void

    var body: some View {
        let dataKey = widget.dataKey ?? ""
        WidgetErrorWrapper(
            widgetTitle: widget.title ?? widget.type,
            isDataFailed: false
        ) {
            let widgetData = allWidgetData[dataKey]
            let context = WidgetRenderContext(
                widget: widget,
                widgetData: widgetData,
                allWidgetData: allWidgetData,
                detailsFormData: detailsFormData,
                detailsData: detailsData,
                onInputChange: onInputChange,
                onResetDetailsFormData: onResetDetailsFormData,
                onAction: onAction
            )
            if let renderer = WidgetRegistry.shared.get(widget.type) {
                renderer(context)
            } else {
                DefaultWidgetView(context: context)
            }
        }
    }
}

/// Unknown widget type fallback.
private struct DefaultWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.title2)
                    .foregroundColor(.red)
                Text("Widget Error")
                    .font(.headline)
            }
            Text("There was an error loading this widget.")
                .font(.subheadline)
                .foregroundColor(.secondary)
            Text("Unknown type: \(context.widget.type)")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color.red.opacity(0.3), lineWidth: 1)
        )
    }
}
