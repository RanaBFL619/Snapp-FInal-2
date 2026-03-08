import SwiftUI
import shared

struct GoogleMapWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Google Map")
    }
}
