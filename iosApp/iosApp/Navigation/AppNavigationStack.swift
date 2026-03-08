import SwiftUI

/// Centralized navigation state for the authenticated app (single source of truth for the stack).
/// Used in: AppShell (creates it, passes via .environmentObject(router)); BottomNavBar & SideDrawer (popToRoot + push);
/// TopToolbarContent (push from user menu); GenericPageView, RecordDetailView, ReportsView, NotFoundView (push/pop).
/// NavigationStack(path: router.pathBinding()) keeps the system back button in sync with `path`.
@MainActor
final class AppNavigationStack: ObservableObject {

    @Published private(set) var path: [Route] = []

    func push(_ route: Route) {
        path.append(route)
    }

    func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
    }

    func popToRoot() {
        path.removeAll()
    }

    /// For deep linking or KMP: replace the entire stack (e.g. after login to default page).
    func replacePath(_ newPath: [Route]) {
        path = newPath
    }

    /// Binding for use with NavigationStack(path:) so system back button updates our path.
    func pathBinding() -> Binding<[Route]> {
        Binding(
            get: { self.path },
            set: { self.path = $0 }
        )
    }
}
