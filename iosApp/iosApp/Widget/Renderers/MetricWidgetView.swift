import SwiftUI
import shared

struct MetricWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Metric")
    }
}
