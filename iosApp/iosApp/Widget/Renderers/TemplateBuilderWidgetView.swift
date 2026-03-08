import SwiftUI
import shared

struct TemplateBuilderWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Template Builder")
    }
}
