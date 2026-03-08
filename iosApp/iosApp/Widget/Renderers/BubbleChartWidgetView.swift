import SwiftUI
import shared

struct BubbleChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Bubble Chart")
    }
}
