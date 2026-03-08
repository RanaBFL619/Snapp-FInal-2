import Foundation
import shared

struct MobileGridItem: Identifiable {
    let id: String
    let widget: PageWidget
    let x: Int
    let y: Int
    let w: Int
    let h: Int
}

/// Converts a desktop-grid widget list into a mobile-friendly layout.
/// On mobile we use full-width stacking (same as web's generateFullWidthLayout / mode "fullWidth"):
/// each widget gets full width (w=gridColumns), preserves API height (h), and stacks vertically (y accumulated).
/// Rendered height = h * rowHeight (from page layout) so proportions match the web.
enum LayoutHelper {

    static func generateMobileLayout(widgets: [PageWidget], gridColumns: Int) -> [MobileGridItem] {
        guard gridColumns > 0 else { return [] }
        print("[Snapp] LayoutHelper | input widgets: \(widgets.count) | gridColumns: \(gridColumns)")

        let sorted = widgets.sorted { w1, w2 in
            let r1 = w1.gridRow?.intValue ?? 0
            let r2 = w2.gridRow?.intValue ?? 0
            let c1 = w1.gridColumn?.intValue ?? 0
            let c2 = w2.gridColumn?.intValue ?? 0
            return r1 != r2 ? r1 < r2 : c1 < c2
        }

        var result: [MobileGridItem] = []
        var currentY = 0
        for widget in sorted {
            let h = max(1, widget.height?.intValue ?? 1)
            result.append(MobileGridItem(
                id: widget.id,
                widget: widget,
                x: 0,
                y: currentY,
                w: gridColumns,
                h: h
            ))
            currentY += h
        }
        print("[Snapp] LayoutHelper | generated items: \(result.count)")
        return result
    }
}
