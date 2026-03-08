import SwiftUI
import shared

struct LineChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Line Chart")
    }
}
