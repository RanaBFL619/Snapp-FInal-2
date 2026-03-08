import SwiftUI
import shared

struct RichTextDisplayWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Rich Text Display")
    }
}
