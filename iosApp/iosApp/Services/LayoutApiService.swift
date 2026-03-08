import Foundation
import shared

// MARK: - API response (matches GET /meta/layout)

private struct LayoutApiResponse: Decodable {
    let navbar: NavbarApi?

    struct NavbarApi: Decodable {
        let location: String?
        let logoText: String?
        let logoUrl: String?
        let nav: [NavItemApi]?
        let userMenu: [UserMenuItemApi]?
    }

    struct NavItemApi: Decodable {
        let icon: String?
        let label: String?
        let route: String?
        let description: String?
        let children: [NavItemApi]?
    }

    struct UserMenuItemApi: Decodable {
        let icon: String?
        let label: String?
        let route: String?
    }
}

// MARK: - Route normalization (same as web: strip /page prefix)

private func normalizeRoute(_ route: String) -> String {
    if route.hasPrefix("/page/") {
        return String(route.dropFirst("/page/".count))
    }
    if route == "/page" {
        return ""
    }
    return route.hasPrefix("/") ? String(route.dropFirst()) : route
}

// MARK: - Map API response to LayoutData

private func mapToLayoutData(_ response: LayoutApiResponse) -> LayoutData {
    let nav = response.navbar?.nav ?? []
    let userMenu = response.navbar?.userMenu ?? []
    return LayoutData(
        logoText: response.navbar?.logoText ?? "",
        logoUrl: response.navbar?.logoUrl ?? "",
        navItems: nav.map { mapNavItem($0) },
        userMenuItems: userMenu.map { item in
            let route = item.route ?? ""
            return UserMenuItemData(
                id: normalizeRoute(route),
                icon: item.icon,
                label: item.label ?? "",
                route: normalizeRoute(route)
            )
        }
    )
}

private func mapNavItem(_ item: LayoutApiResponse.NavItemApi) -> NavItemData {
    let label = item.label ?? ""
    let route = normalizeRoute(item.route ?? "")
    return NavItemData(
        id: route.isEmpty ? label : route,
        icon: item.icon,
        label: label,
        route: route.isEmpty ? label.lowercased() : route,
        children: (item.children ?? []).map { mapNavItem($0) }
    )
}

// MARK: - Fetch layout from backend (same as web client)

private let layoutBaseURL = "https://snapp-blue-river.fly.dev"

func fetchLayoutFromAPI(authToken: String?) async throws -> LayoutData {
    guard let token = authToken, !token.isEmpty else {
        throw LayoutApiError.notAuthenticated
    }
    let url = URL(string: "\(layoutBaseURL)/meta/layout")!
    var request = URLRequest(url: url)
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.setValue("application/json", forHTTPHeaderField: "Accept")
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
    request.httpMethod = "GET"

    let (data, response) = try await URLSession.shared.data(for: request)
    guard let http = response as? HTTPURLResponse else {
        throw LayoutApiError.invalidResponse
    }
    if http.statusCode == 401 {
        SnappKoin.shared.getAuthViewModel().logout()
        throw LayoutApiError.httpStatus(401)
    }
    guard (200...299).contains(http.statusCode) else {
        throw LayoutApiError.httpStatus(http.statusCode)
    }
    // Empty body or empty array → valid empty layout (same as web when API returns minimal data)
    let isEmptyArray = data.count == 2 && data[0] == 0x5B && data[1] == 0x5D // "[]"
    if data.isEmpty || isEmptyArray {
        return LayoutData(logoText: "", logoUrl: "", navItems: [], userMenuItems: [])
    }
    let decoder = JSONDecoder()
    do {
        let apiResponse = try decoder.decode(LayoutApiResponse.self, from: data)
        return mapToLayoutData(apiResponse)
    } catch {
        throw LayoutApiError.decodeError(error)
    }
}

enum LayoutApiError: Error, LocalizedError {
    case notAuthenticated
    case invalidResponse
    case httpStatus(Int)
    case decodeError(Error)
    var errorDescription: String? {
        switch self {
        case .notAuthenticated: return "Not authenticated. Please sign in again."
        case .invalidResponse: return "Invalid response from server"
        case .httpStatus(let code): return "Server error (HTTP \(code))"
        case .decodeError(let e): return "Invalid layout data: \(e.localizedDescription)"
        }
    }
}
