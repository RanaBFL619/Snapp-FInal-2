import SwiftUI

struct RecordDetailView: View {
    @EnvironmentObject var router: AppNavigationStack
    let recordId: String

    var body: some View {
        VStack(spacing: 20) {
            Text("Record: \(recordId)")
                .font(.title2)
            Button("Back") {
                router.pop()
            }
            .buttonStyle(.bordered)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
