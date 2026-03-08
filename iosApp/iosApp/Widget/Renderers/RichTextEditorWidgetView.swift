import SwiftUI
import shared

struct RichTextEditorWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Rich Text Editor")
    }
}
