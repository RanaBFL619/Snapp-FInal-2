import SwiftUI
import shared

struct FieldWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Field")
    }
}
