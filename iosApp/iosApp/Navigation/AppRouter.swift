import Foundation

/// Type-safe routes for the authenticated zone (matches web AppRouter: path ":entity" and "record/:id").
/// Any slug from the layout API (dashboard, customers, orders, etc.) uses .entity(slug).
enum Route: Hashable {
    /// Dynamic entity (matches web :entity): dashboard, customers, orders, or any future route from layout API.
    case entity(slug: String)
    case record(id: String)
    case reports
    case notFound
}

/// All destinations reachable within the unauthenticated stack.
enum UnauthRoute: Hashable {
    case forgotPassword
    case resetPassword(token: String)
}
