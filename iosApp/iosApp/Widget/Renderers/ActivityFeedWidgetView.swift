import SwiftUI
import shared

struct ActivityFeedWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Activity Feed")
    }
}
