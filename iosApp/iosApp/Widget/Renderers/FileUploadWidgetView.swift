import SwiftUI
import shared

struct FileUploadWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "File Upload")
    }
}
