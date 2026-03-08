import SwiftUI
import shared

struct GanttChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Gantt Chart")
    }
}
