import SwiftUI
import shared

struct CalendarWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Calendar")
    }
}
