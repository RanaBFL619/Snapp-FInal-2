import SwiftUI
import shared

struct ContactWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Contact")
    }
}
