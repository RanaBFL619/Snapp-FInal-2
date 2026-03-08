import SwiftUI

/// Root entry point. Reads central auth store (AuthViewModel → shared AuthSharedViewModel).
/// On launch, store is checked (persisted session restored); if auth present → logged in → AppShell loads layout API and shows dynamic nav.
/// UI updates reactively when store changes (login/logout/401); no prop drilling — store is accessible via @EnvironmentObject.
struct RootView: View {
    @StateObject private var authVM = AuthViewModel()
    @StateObject private var toast = ToastManager()

    var body: some View {
        ZStack(alignment: .top) {
            Group {
                switch authVM.authState {
                case .loggedIn:
                    AppShell()
                        .environmentObject(authVM)
                case .loading:
                    ProgressView("Signing in…")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                case .idle, .error:
                    LoginView()
                        .environmentObject(authVM)
                }
            }
            .environmentObject(toast)

            ToastOverlay(message: toast.message, isSuccess: toast.isSuccess)
        }
        .onChange(of: authVM.authState) { newState in
            if case .loggedIn = newState {
                toast.show("Logged in successfully", isSuccess: true)
            }
        }
    }
}
