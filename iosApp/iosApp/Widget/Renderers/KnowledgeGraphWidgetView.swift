import SwiftUI
import shared

struct KnowledgeGraphWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Knowledge Graph")
    }
}
