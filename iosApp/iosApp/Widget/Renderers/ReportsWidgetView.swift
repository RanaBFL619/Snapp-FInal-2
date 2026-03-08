import SwiftUI
import shared

struct ReportsWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Report")
    }
}
