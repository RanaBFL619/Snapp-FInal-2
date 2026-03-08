import SwiftUI

struct SideDrawer: View {
    let layout: LayoutData
    let currentRoute: String
    let userName: String
    let userEmail: String
    let onItemTap: (NavItemData) -> Void
    let onClose: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            VStack(spacing: 0) {
                // Header
                HStack(alignment: .center, spacing: 12) {
                    // Avatar
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.2))
                            .frame(width: 48, height: 48)
                        Text(String(userName.first?.uppercased() ?? "U"))
                            .font(.title2.bold())
                            .foregroundColor(.white)
                    }

                    VStack(alignment: .leading, spacing: 2) {
                        Text(userName.isEmpty ? "Username" : userName)
                            .font(.subheadline.bold())
                            .foregroundColor(.white)
                            .lineLimit(1)
                        Text(userEmail)
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.8))
                            .lineLimit(1)
                    }

                    Spacer()

                    Button(action: onClose) {
                        Image(systemName: "xmark")
                            .foregroundColor(.white)
                            .font(.system(size: 14, weight: .semibold))
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 20)
                .background(Color.accentColor)

                Divider()

                // Nav items
                List {
                    ForEach(layout.navItems) { item in
                        NavDrawerRow(
                            item: item,
                            currentRoute: currentRoute,
                            depth: 0,
                            onTap: onItemTap
                        )
                    }
                }
                .listStyle(.plain)
            }
            .frame(width: 280)
            .background(Color(.systemBackground))
            .shadow(color: .black.opacity(0.15), radius: 8, x: 4, y: 0)

            Spacer()
                .contentShape(Rectangle())
                .onTapGesture { onClose() }
        }
    }
}

private struct NavDrawerRow: View {
    let item: NavItemData
    let currentRoute: String
    let depth: Int
    let onTap: (NavItemData) -> Void

    @State private var expanded = false

    private var isSelected: Bool { currentRoute == item.route }
    private var hasChildren: Bool { !item.children.isEmpty }
    private var indent: CGFloat { CGFloat(depth * 12) }

    var body: some View {
        VStack(spacing: 0) {
            Button {
                if hasChildren {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        expanded.toggle()
                    }
                } else {
                    onTap(item)
                }
            } label: {
                HStack(spacing: 14) {
                    Image(systemName: systemIconName(item.icon))
                        .font(.system(size: 16))
                        .foregroundColor(isSelected ? .accentColor : .secondary)
                        .frame(width: 20)

                    Text(item.label)
                        .font(.body)
                        .fontWeight(isSelected ? .semibold : .regular)
                        .foregroundColor(isSelected ? .accentColor : .primary)

                    Spacer()

                    if hasChildren {
                        Image(systemName: expanded ? "chevron.up" : "chevron.down")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .padding(.leading, indent)
                .padding(.vertical, 10)
                .padding(.horizontal, 16)
                .background(isSelected ? Color.accentColor.opacity(0.10) : Color.clear)
                .cornerRadius(8)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            if hasChildren && expanded {
                ForEach(item.children) { child in
                    NavDrawerRow(
                        item: child,
                        currentRoute: currentRoute,
                        depth: depth + 1,
                        onTap: onTap
                    )
                }
            }
        }
        .listRowInsets(EdgeInsets())
        .listRowSeparator(.hidden)
        .listRowBackground(Color.clear)
    }

    private func systemIconName(_ icon: String?) -> String {
        switch icon?.lowercased() {
        case "dashboard", "home": return "house"
        case "account", "accounts", "customer", "customers", "person", "user": return "person.2"
        case "orders", "list", "records": return "list.bullet"
        case "reports", "report": return "chart.bar"
        case "projects", "project": return "folder"
        case "opportunities": return "lightbulb"
        default: return "circle.grid.2x2"
        }
    }
}
