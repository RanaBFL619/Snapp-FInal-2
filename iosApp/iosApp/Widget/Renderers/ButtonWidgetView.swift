import SwiftUI
import shared

struct ButtonWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Button")
    }
}
