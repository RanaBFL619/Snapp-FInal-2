import SwiftUI
import shared

/// Renders "list" type from API (web: TableWidget).
struct TableWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "List")
    }
}
