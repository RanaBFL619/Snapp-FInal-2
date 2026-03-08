import SwiftUI

struct ReportsView: View {
    @EnvironmentObject var router: AppNavigationStack

    var body: some View {
        VStack(spacing: 20) {
            Text("Reports")
                .font(.title2)
            Button("Back") {
                router.pop()
            }
            .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
