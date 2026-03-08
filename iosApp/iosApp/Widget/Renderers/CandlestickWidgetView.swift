import SwiftUI
import shared

struct CandlestickWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Candle Chart")
    }
}
