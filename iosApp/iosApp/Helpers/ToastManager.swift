import SwiftUI

// MARK: - Login error sanitizer (never show technical messages to the user)
enum LoginErrorSanitizer {
    static let genericMessage = "Invalid email or password. Please try again."
    static let networkMessage = "No internet connection. Please check and try again."
    static let serverMessage = "Something went wrong. Please try again later."

    /// Returns a user-friendly message. Replaces any technical/backend text so the user never sees it.
    static func userFriendly(_ raw: String) -> String {
        let lower = raw.lowercased()
        if lower.contains("unable to resolve") || lower.contains("timeout") || lower.contains("connection") { return networkMessage }
        if lower.contains("500") || lower.contains("502") || lower.contains("internal server") { return serverMessage }
        if lower.contains("serial") || lower.contains("serial name") || lower.contains("required for type")
            || lower.contains("missing at path") || lower.contains("path:") || lower.contains("com.snapp")
            || lower.contains("loginresponse") || lower.contains("token") && lower.contains("username")
            || lower.contains("fields") && lower.contains("required") || lower.contains("serializer")
            || lower.contains("decode") || lower.contains("kotlinx") {
            return genericMessage
        }
        if lower.contains("401") || lower.contains("unauthorized") || lower.contains("invalid credentials") { return genericMessage }
        return genericMessage
    }
}

/// Reusable toast message manager. Use via `@EnvironmentObject` and call `show(_:)` to display a message.
/// The message is shown in an overlay and auto-dismisses after a short delay. Use for login feedback, errors, etc.
final class ToastManager: ObservableObject {
    @Published private(set) var message: String?
    @Published private(set) var isSuccess: Bool = true

    private var dismissTask: Task<Void, Never>?

    /// Show a toast message. Replaces any current message. Use `isSuccess: false` for error styling.
    func show(_ text: String, isSuccess: Bool = true) {
        dismissTask?.cancel()
        message = text
        self.isSuccess = isSuccess
        dismissTask = Task { @MainActor in
            try? await Task.sleep(nanoseconds: 2_500_000_000)
            guard !Task.isCancelled else { return }
            withAnimation(.easeOut(duration: 0.2)) {
                message = nil
            }
        }
    }

    func dismiss() {
        dismissTask?.cancel()
        message = nil
    }
}

// MARK: - Toast overlay view (use in root overlay)
struct ToastOverlay: View {
    let message: String?
    let isSuccess: Bool

    var body: some View {
        Group {
            if let message = message, !message.isEmpty {
                Text(message)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .background(isSuccess ? Color.green : Color.red.opacity(0.9))
                    .cornerRadius(10)
                    .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 4)
                    .padding(.horizontal, 24)
                    .padding(.top, 56)
                    .transition(.move(edge: .top).combined(with: .opacity))
                    .zIndex(100)
            }
        }
        .animation(.easeOut(duration: 0.25), value: message)
    }
}
