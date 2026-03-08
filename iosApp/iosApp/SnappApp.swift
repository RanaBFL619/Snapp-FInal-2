import SwiftUI
import shared

@main
struct SnappApp: App {
    init() {
        IosKoinSetup.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
