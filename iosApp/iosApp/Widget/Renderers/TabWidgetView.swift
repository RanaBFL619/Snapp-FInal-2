import SwiftUI
import shared

/// Tabs container — iOS counterpart of client TabWidget.tsx.
///
/// **Web (TabWidget.tsx):**
/// - tabs = widget.tabs ?? [], tabContents = widget.components ?? []
/// - Tabs defaultValue={tabs[0]?.id || "details"}, TabsTrigger value=tab.id label=tab.label
/// - TabsContent value=tabContent.id, content = WidgetRenderer(widget={tabContent})
/// - Matching: selected tab id must equal the content pane id (tab.id ↔ tabContent.id); API may use tabId on components.
///
/// **iOS:** tabsOrFromOptions() for labels, context.widget.components for content. contentForTab(id) matches by component.id or getTabId() (options.tabId). Full context passed so record/field inside tabs work.
struct TabWidgetView: View {
    let context: WidgetRenderContext

    @State private var selectedTabId: String = ""

    private var tabs: [TabInfo] {
        Array(context.widget.tabsOrFromOptions())
    }

    private var components: [PageWidget] {
        Array(context.widget.components ?? [])
    }

    /// Web: defaultValue = tabs[0]?.id || "details"
    private var defaultTabId: String {
        tabs.first?.id ?? components.first?.id ?? components.first?.getTabId() ?? "details"
    }

    /// Web: TabsContent value=tabContent.id; we match by component.id or options.tabId (getTabId()).
    private func contentForTab(id: String) -> PageWidget? {
        components.first { $0.id == id }
            ?? components.first { $0.getTabId() == id }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if tabs.isEmpty && components.isEmpty {
                emptyState
            } else if tabs.isEmpty {
                tabContent(widget: components.first)
            } else {
                tabStripAndContent
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .onAppear {
            if selectedTabId.isEmpty { selectedTabId = defaultTabId }
        }
    }

    private var emptyState: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(context.widget.title ?? "Tabs")
                .font(.headline)
                .foregroundColor(.secondary)
            Text("No tabs or content")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
    }

    private var tabStripAndContent: some View {
        let binding = Binding(
            get: { selectedTabId.isEmpty ? defaultTabId : selectedTabId },
            set: { selectedTabId = $0 }
        )
        return VStack(alignment: .leading, spacing: 8) {
            tabList(selectedId: binding.wrappedValue) { tab in
                selectedTabId = tab.id
            }
            contentArea(selectedTabId: binding.wrappedValue)
        }
    }

    private func tabList(selectedId: String, onSelect: @escaping (TabInfo) -> Void) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                ForEach(tabs, id: \.id) { tab in
                    Button(tab.label) { onSelect(tab) }
                        .buttonStyle(.bordered)
                        .tint(selectedId == tab.id ? Color.accentColor : Color.gray)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
        .background(Color(UIColor.secondarySystemBackground))
    }

    @ViewBuilder
    private func contentArea(selectedTabId: String) -> some View {
        if let widget = contentForTab(id: selectedTabId) {
            tabContent(widget: widget)
        } else {
            Text("No content for this tab")
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(maxWidth: .infinity)
                .padding()
        }
    }

    @ViewBuilder
    private func tabContent(widget: PageWidget?) -> some View {
        if let w = widget {
            WidgetHost(
                widget: w,
                allWidgetData: context.allWidgetData,
                detailsFormData: context.detailsFormData,
                detailsData: context.detailsData,
                onInputChange: context.onInputChange,
                onResetDetailsFormData: context.onResetDetailsFormData,
                onAction: context.onAction
            )
            .padding(12)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(UIColor.separator), lineWidth: 1)
            )
        } else {
            EmptyView()
        }
    }
}
