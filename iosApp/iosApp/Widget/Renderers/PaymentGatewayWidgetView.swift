import SwiftUI
import shared

struct PaymentGatewayWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Payment Gateway")
    }
}
