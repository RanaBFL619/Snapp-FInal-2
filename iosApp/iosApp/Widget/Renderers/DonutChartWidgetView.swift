import SwiftUI
import shared

struct DonutChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Donut Chart")
    }
}
