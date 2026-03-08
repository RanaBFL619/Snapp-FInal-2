import SwiftUI
import shared

struct PieChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Pie Chart")
    }
}
