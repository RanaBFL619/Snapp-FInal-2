import SwiftUI
import shared

/// Swift value type mirroring KMM `UserSession`.
struct UserSessionData {
    let token: String
    let name: String?
    let username: String
    let roles: [String]
    let defaultPage: String?
}

/// Swift wrapper for the central auth store (shared AuthSharedViewModel).
/// Auth state is the single source of truth; when it changes, SwiftUI re-renders.
/// Access via @EnvironmentObject(authVM) so any screen can read auth without prop drilling.
@MainActor
final class AuthViewModel: ObservableObject {

    enum AuthState: Equatable {
        case idle
        case loading
        case loggedIn
        case error(String)
    }

    @Published private(set) var authState: AuthState = .idle
    @Published private(set) var userSession: UserSessionData? = nil

    private let sharedVM: AuthSharedViewModel

    init() {
        sharedVM = SnappKoin.shared.getAuthViewModel()
        // Shared VM restores session in its init and emits it via snapshot. Show loading until first snapshot to avoid flashing Login.
        authState = .loading
        sharedVM.collectAuthStateSnapshot { [weak self] snapshot in
            DispatchQueue.main.async {
                self?.updateState(from: snapshot)
            }
        }
    }

    func login(username: String, password: String) {
        sharedVM.login(username: username, password: password)
    }

    func logout() {
        sharedVM.logout()
    }

    func clearError() {
        sharedVM.clearError()
    }

    // MARK: - Private

    private func updateState(from snapshot: AuthStateSnapshot) {
        switch snapshot.kind {
        case "success":
            if let s = snapshot.session {
                userSession = UserSessionData(
                    token: s.token,
                    name: s.name,
                    username: s.username,
                    roles: Array(s.roles),
                    defaultPage: s.defaultPage
                )
            }
            authState = .loggedIn
        case "loading":
            authState = .loading
        case "error":
            userSession = nil
            authState = .error(snapshot.errorMessage ?? "Error")
        default:
            userSession = nil
            authState = .idle
        }
    }
}
