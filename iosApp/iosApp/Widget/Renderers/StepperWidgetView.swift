import SwiftUI
import shared

struct StepperWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "Stepper")
    }
}
