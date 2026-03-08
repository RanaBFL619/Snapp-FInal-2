import SwiftUI

/// Wraps a widget so only this widget shows an error when its data failed to load (API failure).
/// Keeps the rest of the page intact; no full-page error.
struct WidgetErrorWrapper<Content: View>: View {
    let widgetTitle: String
    let isDataFailed: Bool
    @ViewBuilder let content: () -> Content

    var body: some View {
        if isDataFailed {
            widgetErrorFallback
        } else {
            content()
        }
    }

    private var widgetErrorFallback: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(widgetTitle)
                .font(.subheadline)
                .fontWeight(.medium)
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.body)
                    .foregroundColor(.orange)
                Text("Couldn't load data for this widget.")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(8)
        }
    }
}
