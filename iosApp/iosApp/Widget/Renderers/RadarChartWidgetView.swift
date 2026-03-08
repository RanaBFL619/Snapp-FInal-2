import SwiftUI
import shared

struct RadarChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Radar Chart")
    }
}
