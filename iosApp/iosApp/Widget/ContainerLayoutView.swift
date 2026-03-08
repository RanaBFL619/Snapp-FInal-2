import SwiftUI
import shared

/// Renders layout items in a row-based grid: groups by row (y), applies width/height from layout
/// so they match web's ResponsiveGridLayout (rowHeight in points = h * rowHeight). Use from GenericPageView, CardWidgetView, AccordionWidgetView.
struct ContainerLayoutView<Content: View>: View {
    let layoutItems: [MobileGridItem]
    let gridColumns: Int
    /// Row height in points (same as web rowHeight). Each item gets minHeight = item.h * rowHeight.
    let rowHeight: CGFloat
    @ViewBuilder let content: (PageWidget) -> Content

    init(layoutItems: [MobileGridItem], gridColumns: Int, rowHeight: CGFloat = 80, @ViewBuilder content: @escaping (PageWidget) -> Content) {
        self.layoutItems = layoutItems
        self.gridColumns = gridColumns
        self.rowHeight = rowHeight
        self.content = content
    }

    private var rows: [(y: Int, items: [MobileGridItem])] {
        let byRow = Dictionary(grouping: layoutItems) { $0.y }
        return byRow.keys.sorted().map { y in (y: y, items: byRow[y]!.sorted { $0.x < $1.x }) }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            ForEach(rows, id: \.y) { row in
                let rowHeightPoints = row.items.map { CGFloat($0.h) * rowHeight }.max() ?? rowHeight
                HStack(alignment: .top, spacing: 10) {
                    ForEach(row.items) { item in
                        content(item.widget)
                            .frame(maxWidth: .infinity, minHeight: CGFloat(item.h) * rowHeight)
                    }
                }
                .frame(minHeight: rowHeightPoints)
            }
        }
    }
}
