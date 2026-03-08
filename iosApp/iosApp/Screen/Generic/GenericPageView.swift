import SwiftUI
import shared

/// Generic page — iOS counterpart of client genericPage.tsx.
///
/// **React flow (web):**
/// genericPage → maps components → GridItemContainer(widget={ ...component, detailsFormData, handleInputChange, resetDetailsFormData })
///   → GridItem(widget) → WidgetRenderer(widget) → widgetRegistry.get(widget.type)(widget)
///
/// **Swift flow (this view):**
/// GenericPageView → layoutItems from LayoutHelper → ContainerLayoutView → for each widget:
///   WidgetHost(widget, allWidgetData, detailsFormData, detailsData: nil, onInputChange, onResetDetailsFormData, onAction)
///   → WidgetHost builds WidgetRenderContext (same as web’s combined widget prop) → WidgetRegistry.get(widget.type)(context) or DefaultWidgetView
///
/// Same data: pageConfig.components, detailsFormData, handleInputChange, resetDetailsFormData. Layout: LayoutHelper + ContainerLayoutView replace react-grid-layout.
struct GenericPageView: View {
    @StateObject private var viewModel = GenericPageViewModel()
    @EnvironmentObject var router: AppNavigationStack
    let slug: String

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                switch viewModel.uiState {
                case .loading:
                    HStack {
                        Spacer()
                        ProgressView()
                            .padding(.vertical, 60)
                        Spacer()
                    }

                case .success:
                    if let page = viewModel.pageConfig {
                        if !page.title.isEmpty {
                            Text(page.title)
                                .font(.title2)
                                .fontWeight(.semibold)
                                .padding(.horizontal)
                        }

                        let columns = Int(page.layout?.columns ?? Int32(12))
                        let rowHeight: CGFloat = {
                            guard let rh = page.layout?.rowHeight else { return 80 }
                            return CGFloat(Int(truncating: rh as NSNumber))
                        }()
                        let layoutItems = LayoutHelper.generateMobileLayout(
                            widgets: Array(page.components),
                            gridColumns: columns
                        )

                        if layoutItems.isEmpty && !page.components.isEmpty {
                            Text("No layout found for widgets.")
                                .foregroundColor(.secondary)
                                .padding()
                        } else {
                            ContainerLayoutView(layoutItems: layoutItems, gridColumns: columns, rowHeight: rowHeight) { widget in
                                WidgetHost(
                                    widget: widget,
                                    allWidgetData: viewModel.widgetData,
                                    detailsFormData: viewModel.detailsFormData,
                                    detailsData: nil,
                                    onInputChange: viewModel.handleInputChange,
                                    onResetDetailsFormData: viewModel.resetDetailsFormData,
                                    onAction: handleWidgetAction
                                )
                            }
                            .padding(.horizontal)
                        }
                    }
                }
            }
            .padding(.vertical)
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.loadPage(slug: slug)
        }
        .onChange(of: slug) { newSlug in
            viewModel.loadPage(slug: newSlug)
        }
    }

    private func handleWidgetAction(_ action: WidgetAction) {
        switch action {
        case .loadPage(let dataKey, let page):
            print("Load page \(page) for dataKey \(dataKey)")
        case .openRecord(let id):
            router.push(.record(id: id))
        }
    }
}
