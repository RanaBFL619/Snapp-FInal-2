import SwiftUI
import shared

struct ImportWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Importer")
    }
}
