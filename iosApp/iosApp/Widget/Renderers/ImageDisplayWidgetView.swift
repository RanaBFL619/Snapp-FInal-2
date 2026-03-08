import SwiftUI
import shared

struct ImageDisplayWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Image")
    }
}
