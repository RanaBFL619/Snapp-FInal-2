import SwiftUI
import shared

struct TodoListWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Todo")
    }
}
