import SwiftUI
import shared

struct HeatMapWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Heatmap")
    }
}
