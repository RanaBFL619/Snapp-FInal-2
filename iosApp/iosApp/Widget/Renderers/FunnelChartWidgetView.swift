import SwiftUI
import shared

struct FunnelChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Funnel Chart")
    }
}
