import SwiftUI

private let maxBottomTabs = 4
// Unselected items: darker gray so labels are readable and bolder (not too light)
private let unselectedColor = Color(red: 0.36, green: 0.36, blue: 0.36)

struct BottomNavBar: View {
    let navItems: [NavItemData]
    let currentRoute: String
    let onItemTap: (NavItemData) -> Void
    let onMoreTap: () -> Void

    private var showMore: Bool { navItems.count > maxBottomTabs }
    private var visibleItems: [NavItemData] {
        showMore ? Array(navItems.prefix(maxBottomTabs)) : navItems
    }

    var body: some View {
        HStack(spacing: 0) {
            ForEach(visibleItems) { item in
                let isSelected = currentRoute == item.route
                Button {
                    onItemTap(item)
                } label: {
                    VStack(spacing: 4) {
                        Image(systemName: systemIconName(item.icon))
                            .font(.system(size: 20, weight: isSelected ? .semibold : .regular))
                            .foregroundColor(isSelected ? .accentColor : unselectedColor)
                        Text(item.label)
                            .font(.system(size: 11, weight: isSelected ? .semibold : .medium))
                            .foregroundColor(isSelected ? .accentColor : unselectedColor)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                    .background(
                        isSelected
                        ? Color.accentColor.opacity(0.08)
                        : Color.clear
                    )
                }
                .buttonStyle(.plain)
            }

            if showMore {
                Button {
                    onMoreTap()
                } label: {
                    VStack(spacing: 4) {
                        Image(systemName: "ellipsis")
                            .font(.system(size: 20))
                            .foregroundColor(unselectedColor)
                        Text("More")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(unselectedColor)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }
                .buttonStyle(.plain)
            }
        }
        .background(
            Color(red: 232/255, green: 232/255, blue: 232/255)
                .ignoresSafeArea(edges: .bottom)
        )
        .overlay(alignment: .top) {
            Rectangle()
                .fill(Color.gray.opacity(0.2))
                .frame(height: 1)
        }
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
