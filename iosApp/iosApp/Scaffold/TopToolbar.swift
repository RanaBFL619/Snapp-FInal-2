import SwiftUI

// Figma: header light gray, content dark
private let headerBarColor = Color(red: 232/255, green: 232/255, blue: 232/255)
private let headerContentColor = Color(red: 31/255, green: 41/255, blue: 55/255)

struct TopToolbarContent: ToolbarContent {
    let logoText: String
    let logoUrl: String
    let userName: String
    let userMenuItems: [UserMenuItemData]
    let onMenuTap: () -> Void
    let onUserMenuItemTap: (UserMenuItemData) -> Void
    let onLogout: () -> Void

    var body: some ToolbarContent {
        ToolbarItem(placement: .navigationBarLeading) {
            HStack(spacing: 8) {
                Button(action: onMenuTap) {
                    Image(systemName: "line.3.horizontal")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(headerContentColor)
                }
                .buttonStyle(.plain)
                if !logoUrl.isEmpty {
                    AsyncImage(url: URL(string: logoUrl)) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                        case .failure:
                            Image(systemName: "building.2")
                                .font(.system(size: 20))
                                .foregroundColor(headerContentColor)
                        case .empty:
                            ProgressView()
                        @unknown default:
                            EmptyView()
                        }
                    }
                    .frame(maxWidth: 80, maxHeight: 32)
                }
//                Text(logoText)
//                    .font(.headline.bold())
//                    .foregroundColor(headerContentColor)
            }
        }

        ToolbarItem(placement: .navigationBarTrailing) {
            HStack(spacing: 4) {
                Menu {
                    ForEach(userMenuItems) { item in
                        Button {
                            if item.label.lowercased() == "logout" {
                                onLogout()
                            } else {
                                onUserMenuItemTap(item)
                            }
                        } label: {
                            Label(item.label, systemImage: systemIconName(item.icon))
                        }
                        .foregroundColor(item.label.lowercased() == "logout" ? .red : .primary)
                    }
                } label: {
                    ZStack {
                        Circle()
                            .fill(headerContentColor.opacity(0.2))
                            .frame(width: 30, height: 30)
                        Text(String(userName.first?.uppercased() ?? "U"))
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(headerContentColor)
                    }
                }
            }
        }
    }

    private func systemIconName(_ icon: String?) -> String {
        switch icon?.lowercased() {
        case "profile", "person", "user": return "person.circle"
        case "settings": return "gearshape"
        case "logout": return "arrow.right.square"
        default: return "person.circle"
        }
    }
}

// Expose header bar color for toolbar background in AppShell
enum SnappLayoutColors {
    static let headerBar = headerBarColor
}
