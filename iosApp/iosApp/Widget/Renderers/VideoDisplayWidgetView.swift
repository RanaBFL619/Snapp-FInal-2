import SwiftUI
import shared

struct VideoDisplayWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Video")
    }
}
