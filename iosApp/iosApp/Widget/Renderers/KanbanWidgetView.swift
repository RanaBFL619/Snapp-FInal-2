import SwiftUI
import shared

struct KanbanWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Kanban")
    }
}
