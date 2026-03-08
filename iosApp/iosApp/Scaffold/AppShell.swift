import SwiftUI

/// Authenticated application shell. Shown only when auth store has session (RootView).
/// Layout is loaded from GET /meta/layout when token is present; nav items, user menu, logo come from API (dynamic like web).
/// Navigation uses each nav item’s route (normalized); no hard-coded tabs.
struct AppShell: View {
    @EnvironmentObject var authVM: AuthViewModel
    @StateObject private var router = AppNavigationStack()
    @StateObject private var layoutVM = LayoutViewModel()

    @State private var isDrawerOpen = false
    @State private var currentRoute: String = "dashboard"

    var body: some View {
        ZStack(alignment: .leading) {
            mainContent
            drawerOverlay
        }
        // Main content and shell respect safe area (status bar/notch at top, home indicator at bottom).
        .onAppear {
            if let token = authVM.userSession?.token, !token.isEmpty {
                layoutVM.loadLayout(authToken: token)
            }
        }
        .onChange(of: authVM.userSession?.token) { newToken in
            if let token = newToken, !token.isEmpty {
                layoutVM.loadLayout(authToken: token)
            }
        }
    }

    // MARK: - Main Content

    @ViewBuilder
    private var mainContent: some View {
        switch layoutVM.layoutState {
        case .loading:
            SkeletonShell()

        case .error(let message):
            LayoutErrorView(message: message) {
                layoutVM.loadLayout(authToken: authVM.userSession?.token)
            }

        case .success(let layout):
            VStack(spacing: 0) {
                NavigationStack(path: router.pathBinding()) {
                    destinationView(for: .entity(slug: "dashboard"))
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.white)
                        .toolbar { toolbarContent(layout: layout) }
                        .navigationDestination(for: Route.self) { route in
                            destinationView(for: route)
                                .toolbar { toolbarContent(layout: layout) }
                                .navigationBarTitleDisplayMode(.inline)
                                .toolbarBackground(SnappLayoutColors.headerBar, for: .navigationBar)
                                .toolbarBackground(.visible, for: .navigationBar)
                        }
                        .navigationBarTitleDisplayMode(.inline)
                        .toolbarBackground(SnappLayoutColors.headerBar, for: .navigationBar)
                        .toolbarBackground(.visible, for: .navigationBar)
                }
                .environmentObject(router)

                // Bottom bar (Figma: same light gray as header)
                BottomNavBar(
                    navItems: layout.navItems,
                    currentRoute: currentRoute,
                    onItemTap: { item in
                        currentRoute = item.route
                        router.popToRoot()
                        if item.route == "reports" {
                            router.push(.reports)
                        } else {
                            router.push(.entity(slug: item.route))
                        }
                    },
                    onMoreTap: {
                        withAnimation(.easeInOut(duration: 0.25)) {
                            isDrawerOpen = true
                        }
                    }
                )
            }
        }
    }

    // MARK: - Drawer Overlay

    @ViewBuilder
    private var drawerOverlay: some View {
        if isDrawerOpen, case .success(let layout) = layoutVM.layoutState {
            Color.black.opacity(0.3)
                .ignoresSafeArea()
                .onTapGesture {
                    withAnimation(.easeInOut(duration: 0.25)) { isDrawerOpen = false }
                }
                .zIndex(10)

            SideDrawer(
                layout: layout,
                currentRoute: currentRoute,
                userName: authVM.userSession?.name ?? authVM.userSession?.username ?? "",
                userEmail: authVM.userSession?.username ?? "",
                onItemTap: { item in
                    currentRoute = item.route
                    withAnimation(.easeInOut(duration: 0.25)) { isDrawerOpen = false }
                    router.popToRoot()
                    if item.route == "reports" {
                        router.push(.reports)
                    } else {
                        router.push(.entity(slug: item.route))
                    }
                },
                onClose: {
                    withAnimation(.easeInOut(duration: 0.25)) { isDrawerOpen = false }
                }
            )
            .transition(.move(edge: .leading))
            .zIndex(11)
        }
    }

    // MARK: - Toolbar (same on every auth screen so header is always visible)

    @ToolbarContentBuilder
    private func toolbarContent(layout: LayoutData) -> some ToolbarContent {
        TopToolbarContent(
            logoText: layout.logoText,
            logoUrl: layout.logoUrl,
            userName: authVM.userSession?.name ?? authVM.userSession?.username ?? "",
            userMenuItems: layout.userMenuItems,
            onMenuTap: { withAnimation(.easeInOut(duration: 0.25)) { isDrawerOpen.toggle() } },
            onUserMenuItemTap: { item in
                router.push(.entity(slug: item.route))
            },
            onLogout: { authVM.logout() }
        )
    }

    // MARK: - Destinations

    @ViewBuilder
    private func destinationView(for route: Route) -> some View {
        Group {
            switch route {
            case .entity(let slug):
                GenericPageView(slug: slug)
            case .record(let id):
                RecordDetailView(recordId: id)
            case .reports:
                ReportsView()
            case .notFound:
                NotFoundView()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.white)
    }
}

// MARK: - Layout error (inlined so it's always in the same target as AppShell)
private struct LayoutErrorView: View {
    let message: String
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text("Failed to Load Layout")
                .font(.title2)
                .fontWeight(.semibold)
            Text("Unable to fetch the application layout from the server.")
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            Button("Retry", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemGroupedBackground))
    }
}
