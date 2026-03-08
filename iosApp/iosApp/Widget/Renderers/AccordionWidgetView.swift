import SwiftUI
import shared

/// Accordion container — iOS counterpart of client AccordionWidget.tsx.
///
/// **Web (AccordionWidget):**
/// - Accordion type="multiple", widget.components.map → AccordionItemComponent per subWidget.
/// - Each subWidget enhanced: detailsData = widget.detailsData || widget.detailsFormData, handleInputChange, isFieldEditable: false.
/// - AccordionItemComponent: if subWidget.components → ResponsiveGridLayout (generateLayoutWithXY, subGridColumn from subWidget.options.layout.columns || parent gridColumn || 1); nested widgets get detailsData/handleInputChange from subWidget or parentWidget. Else single GridItem(subWidget).
/// - Section title: subWidget?.title || "Component {id}".
///
/// **iOS:** One DisclosureGroup per context.widget.components. Nested: LayoutHelper + ContainerLayoutView (same algorithm). Full context passed; detailsData/detailsFormData/onInputChange match web. Column default 1 for nested (web gridColumn || 1).
struct AccordionWidgetView: View {
    let context: WidgetRenderContext

    private var components: [PageWidget] {
        Array(context.widget.components ?? [])
    }

    /// Web: parent gridColumn = widget?.options?.layout?.columns (for nested fallback).
    private var parentGridColumns: Int {
        Int(truncating: context.widget.getLayoutColumns() ?? 1)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if components.isEmpty {
                emptyState
            } else {
                ForEach(components, id: \.id) { component in
                    AccordionItemView(
                        component: component,
                        context: context,
                        parentGridColumns: parentGridColumns
                    )
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var emptyState: some View {
        DisclosureGroup(isExpanded: .constant(true)) {
            Text("No components available")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(maxWidth: .infinity)
                .padding()
        } label: {
            Text(context.widget.title ?? "No Components")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(8)
    }
}

private struct AccordionItemView: View {
    let component: PageWidget
    let context: WidgetRenderContext
    let parentGridColumns: Int

    private var hasNestedComponents: Bool {
        guard let comps = component.components else { return false }
        return !comps.isEmpty
    }

    /// Web: subWidget?.title || `Component ${subWidget?.id}`
    private var sectionTitle: String {
        component.title ?? "Component \(component.id)"
    }

    /// Web: subWidget?.options?.layout?.columns || gridColumn || 1
    private var nestedGridColumns: Int {
        guard let cols = component.getLayoutColumns() else { return max(1, parentGridColumns) }
        return max(1, Int(truncating: cols))
    }

    var body: some View {
        DisclosureGroup {
            if hasNestedComponents {
                let columns = nestedGridColumns
                let layout = LayoutHelper.generateMobileLayout(
                    widgets: Array(component.components!),
                    gridColumns: columns
                )
                ContainerLayoutView(layoutItems: layout, gridColumns: columns) { widget in
                    WidgetHost(
                        widget: widget,
                        allWidgetData: context.allWidgetData,
                        detailsFormData: context.detailsFormData,
                        detailsData: context.detailsData,
                        onInputChange: context.onInputChange,
                        onResetDetailsFormData: context.onResetDetailsFormData,
                        onAction: context.onAction
                    )
                }
                .padding(.top, 4)
            } else {
                WidgetHost(
                    widget: component,
                    allWidgetData: context.allWidgetData,
                    detailsFormData: context.detailsFormData,
                    detailsData: context.detailsData,
                    onInputChange: context.onInputChange,
                    onResetDetailsFormData: context.onResetDetailsFormData,
                    onAction: context.onAction
                )
                .padding(.top, 4)
            }
        } label: {
            Text(sectionTitle)
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color(UIColor.separator), lineWidth: 1)
        )
    }
}
