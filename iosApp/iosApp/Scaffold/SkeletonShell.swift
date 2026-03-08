import SwiftUI

struct SkeletonShell: View {
    @State private var shimmer = false

    var body: some View {
        VStack(spacing: 0) {
            // Top bar skeleton
            HStack {
                SkeletonBlock(width: 28, height: 28, cornerRadius: 4)
                Spacer()
                SkeletonBlock(width: 120, height: 20, cornerRadius: 6)
                Spacer()
                HStack(spacing: 12) {
                    SkeletonBlock(width: 28, height: 28, cornerRadius: 14)
                    SkeletonBlock(width: 28, height: 28, cornerRadius: 14)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color(.systemBackground))
            .shadow(color: .black.opacity(0.05), radius: 2, y: 1)

            // Content skeleton
            ScrollView {
                VStack(spacing: 12) {
                    SkeletonBlock(width: nil, height: 140, cornerRadius: 12)
                    HStack(spacing: 12) {
                        SkeletonBlock(width: nil, height: 80, cornerRadius: 10)
                        SkeletonBlock(width: nil, height: 80, cornerRadius: 10)
                        SkeletonBlock(width: nil, height: 80, cornerRadius: 10)
                    }
                    SkeletonBlock(width: nil, height: 200, cornerRadius: 12)
                    HStack {
                        SkeletonBlock(width: nil, height: 16, cornerRadius: 4)
                            .frame(maxWidth: 240)
                        Spacer()
                    }
                    HStack {
                        SkeletonBlock(width: nil, height: 16, cornerRadius: 4)
                            .frame(maxWidth: 160)
                        Spacer()
                    }
                }
                .padding(16)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Bottom tab bar skeleton
            HStack(spacing: 0) {
                ForEach(0..<5) { _ in
                    VStack(spacing: 4) {
                        SkeletonBlock(width: 24, height: 24, cornerRadius: 4)
                        SkeletonBlock(width: 40, height: 10, cornerRadius: 4)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 10)
            .background(Color(.systemBackground))
            .shadow(color: .black.opacity(0.05), radius: 2, y: -1)
        }
        .background(Color(.systemGroupedBackground))
        .onAppear { shimmer = true }
    }
}

private struct SkeletonBlock: View {
    let width: CGFloat?
    let height: CGFloat
    let cornerRadius: CGFloat

    @State private var phase: CGFloat = 0

    var body: some View {
        RoundedRectangle(cornerRadius: cornerRadius)
            .fill(
                LinearGradient(
                    gradient: Gradient(colors: [
                        Color(.systemFill),
                        Color(.secondarySystemFill),
                        Color(.systemFill)
                    ]),
                    startPoint: UnitPoint(x: phase - 0.5, y: 0.5),
                    endPoint: UnitPoint(x: phase + 0.5, y: 0.5)
                )
            )
            .frame(width: width, height: height)
            .frame(maxWidth: width == nil ? .infinity : nil)
            .onAppear {
                withAnimation(.linear(duration: 1.2).repeatForever(autoreverses: false)) {
                    phase = 1.5
                }
            }
    }
}
