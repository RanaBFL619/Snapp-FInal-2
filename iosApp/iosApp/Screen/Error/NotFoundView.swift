import SwiftUI

struct NotFoundView: View {
    @EnvironmentObject var router: AppNavigationStack

    var body: some View {
        VStack(spacing: 20) {
            Text("404 - Page Not Found")
                .font(.title)
            Button("Back to Dashboard") {
                router.popToRoot()
            }
            .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
