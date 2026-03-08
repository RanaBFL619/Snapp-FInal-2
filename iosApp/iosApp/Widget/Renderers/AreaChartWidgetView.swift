import SwiftUI
import shared

struct AreaChartWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Area Chart")
    }
}
