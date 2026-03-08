import SwiftUI
import shared

struct IFrameWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "i-Frame")
    }
}
