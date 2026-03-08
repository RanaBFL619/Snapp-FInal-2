import SwiftUI

// MARK: - Swift layout types (used by AppShell, SideDrawer, BottomNavBar)
// When using shared layout API, convert Kotlin NavItem/NavbarUserMenuItem in a bridge layer.

struct NavItemData: Identifiable, Hashable {
    let id: String
    let icon: String?
    let label: String
    let route: String
    let children: [NavItemData]
}

struct UserMenuItemData: Identifiable {
    let id: String
    let icon: String?
    let label: String
    let route: String
}

struct LayoutData {
    let logoText: String
    let logoUrl: String
    let navItems: [NavItemData]
    let userMenuItems: [UserMenuItemData]
}

// MARK: - LayoutViewModel (fetches from GET /meta/layout like web client)

/// Drives layout state for the app shell from the backend API.
/// - **Drawer:** Shows all `layout.navItems` (with expandable children).
/// - **Bottom bar:** First 4 items as tabs; 5th "More" opens the drawer when navItems.count > 4.
@MainActor
final class LayoutViewModel: ObservableObject {

    enum LayoutState {
        case loading
        case success(LayoutData)
        case error(String)
    }

    @Published private(set) var layoutState: LayoutState = .loading
    @Published private(set) var layoutData: LayoutData? = nil

    init() {}

    /// Fetches layout from GET /meta/layout (same as web). Pass auth token from AuthViewModel.userSession?.token.
    func loadLayout(authToken: String?) {
        layoutState = .loading
        Task { @MainActor in
            do {
                let data = try await fetchLayoutFromAPI(authToken: authToken)
                layoutData = data
                layoutState = .success(data)
            } catch {
                layoutData = nil
                layoutState = .error(error.localizedDescription)
            }
        }
    }
}
