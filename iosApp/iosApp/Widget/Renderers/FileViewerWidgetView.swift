import SwiftUI
import shared

struct FileViewerWidgetView: View {
    let context: WidgetRenderContext

    var body: some View {
        WidgetNameTemplateView(context: context, displayName: "File Viewer")
    }
}
