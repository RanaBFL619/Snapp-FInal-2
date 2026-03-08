import SwiftUI
import shared

struct PipelineWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Pipeline")
    }
}
