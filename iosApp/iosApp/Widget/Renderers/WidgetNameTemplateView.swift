import SwiftUI
import shared

/// Shared minimal UI for widget placeholders: shows widget type display name and optional title/dataKey.
/// Each widget view receives full `WidgetRenderContext` from parent and can use this for the template UI
/// until real implementation is added.
struct WidgetNameTemplateView: View {
    let context: WidgetRenderContext
    let displayName: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            if let title = context.widget.title, !title.isEmpty {
                Text(title)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.secondary)
            }
            Text(displayName)
                .font(.body)
                .fontWeight(.semibold)
            if let dataKey = context.widget.dataKey, !dataKey.isEmpty {
                Text(dataKey)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(10)
    }
}
