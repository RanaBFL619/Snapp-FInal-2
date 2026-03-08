import SwiftUI
import shared

@MainActor
final class GenericPageViewModel: ObservableObject {
    @Published private(set) var uiState: PageUiState = .loading
    @Published private(set) var pageConfig: PageConfig? = nil
    /// Widget data map: dataKey → raw JSON string (from API via shared layer).
    @Published private(set) var widgetData: [String: String] = [:]
    /// Page-level form state for field/card edit mode (same as web detailsFormData). Keys = field/dataKey, value = current value.
    @Published var detailsFormData: [String: Any] = [:]

    enum PageUiState: Equatable {
        case loading
        case success
    }

    private let sharedVM: PageSharedViewModel
    /// Slug we're showing — only apply snapshots that match so dashboard doesn't show customer data (shared VM is singleton).
    private var currentSlug: String = ""

    init() {
        print("[Snapp] GenericPageViewModel init")
        sharedVM = SnappKoin.shared.getPageSharedViewModel()
        sharedVM.collectPageStateSnapshot { [weak self] snapshot in
            print("[Snapp] snapshot received: \(snapshot.kind)")
            DispatchQueue.main.async {
                self?.updateState(from: snapshot)
            }
        }
    }

    func loadPage(slug: String) {
        currentSlug = slug
        resetDetailsFormData()
        Task {
            do {
                try await sharedVM.loadPage(slug: slug)
            } catch {
                // API failure = no data; show empty page, don't break UI
                pageConfig = nil
                widgetData = [:]
                uiState = .success
            }
        }
    }

    /// Called when a field/widget reports a value change (same as web handleInputChange).
    func handleInputChange(componentId: String, key: String, value: Any) {
        guard !componentId.isEmpty, !key.isEmpty else { return }
        var next = detailsFormData
        next[key] = value
        detailsFormData = next
    }

    /// Clears page-level form state (same as web resetDetailsFormData). Also called when entity/slug changes.
    func resetDetailsFormData() {
        detailsFormData = [:]
    }

    private func updateState(from snapshot: PageUiStateSnapshot) {
        print("[Snapp] updateState | kind=\(snapshot.kind)")
        
        switch snapshot.kind {
        case "success":
            self.pageConfig = snapshot.pageConfig
            print("[Snapp] updateState | success | pageConfig=\(snapshot.pageConfig != nil ? "present" : "nil") | components=\(snapshot.pageConfig?.components.count ?? 0)")
            if let rawData = snapshot.widgetData {
                var bridged: [String: String] = [:]
                rawData.forEach { key, value in bridged[key] = value }
                self.widgetData = bridged
            } else {
                self.widgetData = [:]
            }
            self.uiState = .success
        case "error":
            // Shared should never emit error; if so, show empty page
            print("[Snapp] updateState | error | message: \(snapshot.errorMessage ?? "none")")
            self.pageConfig = nil
            self.widgetData = [:]
            self.uiState = .success
        default:
            self.uiState = .loading
        }
    }
}
