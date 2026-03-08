import SwiftUI
import shared

struct ComprehensiveTableWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Table")
    }
}
