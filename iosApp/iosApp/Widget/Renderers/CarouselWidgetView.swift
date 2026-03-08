import SwiftUI
import shared

struct CarouselWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Carousel")
    }
}
