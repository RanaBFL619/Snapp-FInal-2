import SwiftUI
import shared

struct RadialChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Radial Chart")
    }
}
