import SwiftUI
import shared

struct BarChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Bar Chart")
    }
}
