import SwiftUI
import shared

// MARK: - Models (parsed from options / API)

struct TableColumnConfig: Identifiable {
    var id: String { key }
    let key: String
    let label: String
    let type: String
    let sortable: Bool
}

struct TableActionConfig: Identifiable {
    var id: String { "\(action)-\(label)" }
    let label: String
    let action: String
    let icon: String?
    let route: String?
}

struct TableActionsConfig {
    var toolbar: [TableActionConfig] = []
    var bulk: [TableActionConfig] = []
    var row: [TableActionConfig] = []
}

/// Filter field config from options.filtersConfig (same as web: type, options, placeholder).
struct FilterFieldConfig: Identifiable {
    var id: String { key }
    let key: String
    let type: String  // "dropdown", "text", "date", "daterange"
    let options: [String]
    let placeholder: String?
}

/// Pagination slot: either a page number or ellipsis. Used for unified pill-style pagination.
private enum PaginationSlot {
    case pageNum(Int)
    case ellipsis
    var pageNum: Int? { if case .pageNum(let n) = self { return n }; return nil }
}

/// Renders "table" type from API — full parity with web ComprehensiveTableWidget.
/// Web: widget.actions (Sync/New etc from JSON), options.actionsConfig.toolbar, Filter, Columns, Sort, selection/bulk, row actions, pagination.
/// iOS: Three-dot menu = Sync (refresh) + Columns sheet + options.actionsConfig.toolbar (from JSON). Filter/Sort/selection/bulk/row/pagination match web.
struct ComprehensiveTableWidgetView: View {
    let context: WidgetRenderContext

    @State private var listData: [[String: Any]] = []
    @State private var allColumns: [TableColumnConfig] = []
    @State private var visibleColumns: [TableColumnConfig] = []
    @State private var page: Int = 1
    @State private var pageSize: Int = 10
    @State private var totalRecords: Int = 0
    @State private var totalPages: Int = 0
    @State private var sortBy: String = "createdAt"
    @State private var sortOrder: String = "desc"
    @State private var filters: [String: Any] = [:]
    @State private var searchTerm: String = ""
    @State private var selectedRows: Set<String> = []
    @State private var isFormOpen: Bool = false
    @State private var editingItem: [String: Any]? = nil
    @State private var deleteItem: [String: Any]? = nil
    @State private var isFormLoading: Bool = false
    @State private var isDeleting: Bool = false
    @State private var isFilterOpen: Bool = false
    @State private var dataLoading: Bool = true
    @State private var actionsConfig: TableActionsConfig = TableActionsConfig()
    @State private var searchEnabled: Bool = false
    @State private var selectionEnabled: Bool = false
    @State private var showDeleteConfirm: Bool = false
    @State private var showPageSizePicker: Bool = false
    @State private var showColumnsSheet: Bool = false
    @State private var filtersConfig: [FilterFieldConfig] = []

    private var dataKey: String { context.widget.dataKey ?? "" }
    private var schemaType: String { context.widget.schema ?? context.widget.dataKey ?? context.widget.id }
    private var parentRecordId: String? { context.detailsData?["recordId"] as? String }

    private var activeFiltersCount: Int {
        filters.values.filter { v in
            if let s = v as? String { return !s.isEmpty && s != "All" }
            return true
        }.count
    }

    private var sortableColumns: [TableColumnConfig] {
        visibleColumns.filter { $0.sortable }
    }

    private var sortOptionLabel: String {
        let col = visibleColumns.first { $0.key == sortBy } ?? sortableColumns.first
        let label = col?.label ?? sortBy
        return sortOrder == "asc" ? "\(label) ↑" : "\(label) ↓"
    }

    var body: some View {
        VStack(spacing: 0) {
            if dataLoading {
                loadingView
            } else if listData.isEmpty {
                emptyView
            } else {
                if !selectedRows.isEmpty && selectionEnabled {
                    selectionHeaderView
                } else {
                    headerView
                }
                listContentView
                paginationView
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(Color(UIColor.systemGroupedBackground))
        .onAppear { loadInitial() }
        .overlay(alignment: .bottomTrailing) {
            if showPageSizePicker {
                pageSizeDropdownView
                    .padding(.bottom, 52)
                    .padding(.trailing, 16)
            }
        }
        .background(
            FixedBottomBarHost(
                isActive: !selectedRows.isEmpty && selectionEnabled,
                barContent: { AnyView(selectionFooterView) }
            )
        )
        .sheet(isPresented: $isFormOpen) { formSheet }
        .sheet(isPresented: $isFilterOpen) { filterDrawerSheet }
        .sheet(isPresented: $showColumnsSheet) { columnsVisibilitySheet }
        .confirmationDialog("Delete?", isPresented: $showDeleteConfirm, titleVisibility: .visible) {
            Button("Delete", role: .destructive) { Task { await handleDeleteConfirm() } }
            Button("Cancel", role: .cancel) { showDeleteConfirm = false; deleteItem = nil }
        } message: { Text("This action cannot be undone.") }
    }

    // MARK: - Load

    /// Same as web: when we have dataKey, fetch table data via POST /data/view/{dataKey} on load (web does this in useEffect -> getListingData).
    private func loadInitial() {
        if let jsonString = context.widgetData, !jsonString.isEmpty,
           let data = jsonString.data(using: .utf8),
           let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            if let records = json["records"] as? [[String: Any]] {
                listData = records
            }
            if let meta = json["meta"] as? [String: Any] {
                totalRecords = meta["totalRecords"] as? Int ?? 0
                totalPages = meta["totalPages"] as? Int ?? 0
                page = meta["page"] as? Int ?? meta["currentPage"] as? Int ?? 1
                pageSize = meta["pageSize"] as? Int ?? 10
            }
        }
        parseOptions()
        // Web: useEffect calls getListingData() when widget.dataKey exists → POST /data/view/{dataKey}. Do the same here so we have data.
        if !dataKey.isEmpty {
            Task { await refetch() }
        } else {
            dataLoading = false
        }
    }

    private func parseOptions() {
        searchEnabled = context.widget.getTableSearchEnabled()?.boolValue ?? false
        selectionEnabled = context.widget.getTableSelectionEnabled()?.boolValue ?? false
        if let p = context.widget.getTablePageSize()?.intValue, p > 0 {
            pageSize = p
        }
        if let fieldsJson = context.widget.getTableFieldsJson(),
           let data = fieldsJson.data(using: .utf8),
           let arr = try? JSONSerialization.jsonObject(with: data) as? [[String: Any]] {
            let parsed = arr.compactMap { obj -> TableColumnConfig? in
                let key = (obj["dataKey"] as? String) ?? (obj["key"] as? String) ?? ""
                let label = (obj["title"] as? String) ?? (obj["label"] as? String) ?? key
                let type = (obj["type"] as? String) ?? "text"
                let sortable = (obj["sortable"] as? Bool) ?? true
                return key.isEmpty ? nil : TableColumnConfig(key: key, label: label, type: type, sortable: sortable)
            }
            allColumns = parsed
            if visibleColumns.isEmpty { visibleColumns = parsed }
        }
        if allColumns.isEmpty && !listData.isEmpty, let first = listData.first {
            let fallback = first.keys.filter { $0 != "recordId" && $0 != "id" }.map { TableColumnConfig(key: $0, label: $0, type: "text", sortable: true) }
            allColumns = fallback
            if visibleColumns.isEmpty { visibleColumns = fallback }
        }
        if let actionsJson = context.widget.getTableActionsConfigJson(),
           let data = actionsJson.data(using: .utf8),
           let config = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            actionsConfig.toolbar = parseActions(config["toolbar"])
            actionsConfig.bulk = parseActions(config["bulk"])
            actionsConfig.row = parseActions(config["row"])
        }
        if let filtersJson = context.widget.getTableFiltersConfigJson(),
           let data = filtersJson.data(using: .utf8),
           let dict = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            filtersConfig = dict.compactMap { key, value -> FilterFieldConfig? in
                guard let obj = value as? [String: Any] else { return nil }
                let type = (obj["type"] as? String) ?? "text"
                let opts = (obj["options"] as? [String]) ?? (obj["options"] as? [Any])?.compactMap { $0 as? String } ?? []
                let placeholder = obj["placeholder"] as? String
                return FilterFieldConfig(key: key, type: type, options: opts, placeholder: placeholder)
            }
        }
    }

    private func parseActions(_ value: Any?) -> [TableActionConfig] {
        guard let arr = value as? [[String: Any]] else { return [] }
        return arr.compactMap { obj in
            guard let label = obj["label"] as? String, let action = obj["action"] as? String else { return nil }
            return TableActionConfig(
                label: label,
                action: action,
                icon: obj["icon"] as? String,
                route: obj["route"] as? String
            )
        }
    }

    private func refetch() async {
        guard !dataKey.isEmpty else { return }
        dataLoading = true
        let token = SnappKoin.shared.getStoredSession()?.token
        do {
            let sortArr: [(field: String, direction: String)]? = [(field: sortBy, direction: sortOrder)]
            let (records, meta) = try await dataViewTable(
                authToken: token,
                dataKey: dataKey,
                page: page,
                pageSize: pageSize,
                sort: sortArr,
                filters: filters.isEmpty ? nil : filters,
                search: searchTerm.isEmpty ? nil : searchTerm,
                recordId: parentRecordId
            )
            await MainActor.run {
                listData = records
                totalRecords = meta.totalRecords
                totalPages = meta.totalPages
                pageSize = meta.pageSize
                dataLoading = false
            }
        } catch {
            await MainActor.run { dataLoading = false }
        }
    }

    // MARK: - Helpers

    private func recordId(from row: [String: Any]) -> String {
        (row["recordId"] as? String) ?? (row["id"] as? String) ?? ""
    }

    private func formatCellValue(_ value: Any?, column: TableColumnConfig) -> String {
        guard let value = value else { return "" }
        if let s = value as? String { return s }
        if let n = value as? NSNumber {
            if column.type == "boolean" { return n.boolValue ? "Yes" : "No" }
            if column.type == "currency" { return "$\(n.intValue)" }
            if column.type == "percentage" { return "\(n.intValue)%" }
            return n.stringValue
        }
        if let b = value as? Bool { return b ? "Yes" : "No" }
        return "\(value)"
    }

    private func primaryText(for row: [String: Any]) -> String {
        let firstKey = visibleColumns.first?.key ?? "name"
        return formatCellValue(row[firstKey], column: TableColumnConfig(key: firstKey, label: firstKey, type: "text", sortable: false))
    }

    private func secondaryLines(for row: [String: Any]) -> [String] {
        visibleColumns.dropFirst().prefix(4).compactMap { col in
            let v = row[col.key]
            return v == nil ? nil : "\(col.label): \(formatCellValue(v, column: col))"
        }
    }

    private func statusBadge(for row: [String: Any]) -> String? {
        for col in visibleColumns {
            if col.key.lowercased().contains("status") || col.key.lowercased().contains("type") {
                if let v = row[col.key] { return formatCellValue(v, column: col) }
            }
        }
        return nil
    }

    // MARK: - Actions

    private func handleRowTap(_ row: [String: Any]) {
        let id = recordId(from: row)
        if !id.isEmpty { context.onAction(.openRecord(id: id)) }
    }

    private func handleToolbarAction(_ action: TableActionConfig) {
        switch action.action {
        case "add":
            editingItem = nil
            isFormOpen = true
        case "export":
            Task { await handleExport() }
        default:
            if let route = action.route, !route.isEmpty {
                context.onAction(.openRecord(id: route))
            }
        }
    }

    private func handleRowAction(_ action: TableActionConfig, row: [String: Any]) {
        switch action.action {
        case "edit":
            editingItem = row
            isFormOpen = true
        case "delete":
            deleteItem = row
            showDeleteConfirm = true
        default:
            handleRowTap(row)
        }
    }

    private func handleExport() async {
        guard !selectedRows.isEmpty else { return }
        let selected = listData.filter { selectedRows.contains(recordId(from: $0)) }
        let headers = visibleColumns.map(\.label)
        var csv = headers.joined(separator: ",") + "\n"
        for row in selected {
            let values = visibleColumns.map { col in
                let v = formatCellValue(row[col.key], column: col)
                return v.contains(",") ? "\"\(v)\"" : v
            }
            csv += values.joined(separator: ",") + "\n"
        }
        let filename = "\(dataKey)_export_\(ISO8601DateFormatter().string(from: Date()).prefix(10)).csv"
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(filename)
        try? csv.write(to: url, atomically: true, encoding: .utf8)
        // In a real app you would present share sheet or save to Files
    }

    private func handleFormSubmit(_ formData: [String: Any]) async {
        isFormLoading = true
        let token = SnappKoin.shared.getStoredSession()?.token
        defer { isFormLoading = false }
        do {
            if let edit = editingItem, let rid = recordId(from: edit) as String?, !rid.isEmpty {
                var record = formData
                record["recordId"] = rid
                try await dataUpdate(authToken: token, schemaType: schemaType, records: [record])
            } else {
                try await dataInsert(authToken: token, schemaType: schemaType, records: [formData])
            }
            await MainActor.run {
                isFormOpen = false
                editingItem = nil
                page = 1
                Task { await refetch() }
            }
        } catch {}
    }

    private func handleDeleteConfirm() async {
        guard let item = deleteItem else { showDeleteConfirm = false; return }
        isDeleting = true
        let rid = recordId(from: item)
        let token = SnappKoin.shared.getStoredSession()?.token
        do {
            try await dataDelete(authToken: token, schemaType: schemaType, recordIds: [rid])
            await MainActor.run {
                deleteItem = nil
                showDeleteConfirm = false
                page = 1
                selectedRows.remove(rid)
                Task { await refetch() }
            }
        } catch {}
        isDeleting = false
    }

    private func handleBulkDelete() {
        guard !selectedRows.isEmpty else { return }
        Task {
            let token = SnappKoin.shared.getStoredSession()?.token
            let ids = Array(selectedRows)
            try? await dataDelete(authToken: token, schemaType: schemaType, recordIds: ids)
            await MainActor.run {
                selectedRows.removeAll()
                page = 1
                Task { await refetch() }
            }
        }
    }

    // MARK: - Subviews

    private var loadingView: some View {
        VStack(alignment: .leading, spacing: 12) {
            ForEach(0..<6, id: \.self) { _ in
                RoundedRectangle(cornerRadius: 12)
                    .fill(Color(UIColor.tertiarySystemFill))
                    .frame(height: 88)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(UIColor.systemGroupedBackground))
    }

    private var emptyView: some View {
        VStack(spacing: 12) {
            Text("No Data Available")
                .font(.headline)
                .fontWeight(.semibold)
                .foregroundColor(.primary)
            Text("There are no records to display.")
                .font(.subheadline)
                .foregroundColor(Color(UIColor.secondaryLabel))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 48)
        .background(Color(UIColor.systemGroupedBackground))
    }

    private var headerView: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                if let title = context.widget.title, !title.isEmpty {
                    Text(title)
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.primary)
                }
                Spacer()
                // Three-dot menu: Sync (refresh, like web widget.actions "sync") + toolbar from options.actionsConfig (JSON) + Columns visibility (like web).
                Menu {
                    Button("Sync") { Task { await refetch() } }
                    if !allColumns.isEmpty {
                        Button("Columns") { showColumnsSheet = true }
                    }
                    ForEach(actionsConfig.toolbar) { action in
                        Button(action.label) {
                            handleToolbarAction(action)
                        }
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.title3)
                        .foregroundColor(.primary)
                }
            }
            if searchEnabled {
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(Color(UIColor.tertiaryLabel))
                        .font(.body)
                    TextField("Search \(context.widget.title?.lowercased() ?? "data")...", text: $searchTerm)
                        .onSubmit { page = 1; Task { await refetch() } }
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(Color(UIColor.tertiarySystemFill))
                .cornerRadius(10)
            }
            HStack {
                if !filtersConfig.isEmpty {
                    Button { isFilterOpen = true } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "line.3.horizontal.decrease.circle")
                            Text("Filter")
                            if activeFiltersCount > 0 {
                                Text("\(activeFiltersCount)")
                                    .font(.caption2)
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 5)
                                    .padding(.vertical, 2)
                                    .background(Color.accentColor)
                                    .clipShape(Capsule())
                            }
                        }
                        .font(.subheadline)
                        .foregroundColor(.primary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(UIColor.tertiarySystemFill))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)
                }
                Spacer()
                if !sortableColumns.isEmpty {
                    Menu {
                        Group {
                            ForEach(sortableColumns, id: \.key) { col in
                                Button {
                                    if sortBy == col.key {
                                        sortOrder = sortOrder == "asc" ? "desc" : "asc"
                                    } else {
                                        sortBy = col.key
                                        sortOrder = "asc"
                                    }
                                    page = 1
                                    Task { await refetch() }
                                } label: {
                                    HStack(spacing: 6) {
                                        Text(col.label)
                                        if sortBy == col.key {
                                            Image(systemName: sortOrder == "asc" ? "chevron.up" : "chevron.down")
                                                .font(.caption)
                                        }
                                    }
                                }
                            }
                        }
                        .frame(width: 100)
                    } label: {
                        HStack(spacing: 6) {
                            Text(sortOptionLabel)
                            Image(systemName: "chevron.down")
                                .font(.caption)
                        }
                        .font(.subheadline)
                        .foregroundColor(.primary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(UIColor.tertiarySystemFill))
                        .cornerRadius(8)
                    }
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color(UIColor.systemBackground))
    }

    /// Selection mode header: same white area as search bar (replaces headerView). Title + action buttons only; Deselect/Selected are on the fixed bottom bar.
    private var selectionHeaderView: some View {
        VStack(alignment: .leading, spacing: 16) {
            if let title = context.widget.title, !title.isEmpty {
                Text(title)
                    .font(.title)
                    .fontWeight(.bold)
                    .foregroundColor(.primary)
            }
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 10) {
                    Button {
                        selectedRows = Set(listData.map { recordId(from: $0) })
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "arrow.down.circle")
                                .font(.subheadline)
                            Text("Select All")
                                .font(.subheadline)
                        }
                        .foregroundColor(.accentColor)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(UIColor.systemBackground))
                        .cornerRadius(8)
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(UIColor.separator), lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                    Button { Task { await handleExport() } } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "square.and.arrow.down")
                                .font(.subheadline)
                            Text("Export")
                                .font(.subheadline)
                        }
                        .foregroundColor(.accentColor)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(UIColor.systemBackground))
                        .cornerRadius(8)
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(UIColor.separator), lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                    if !actionsConfig.bulk.isEmpty {
                        HStack(spacing: 6) {
                            Image(systemName: "person.2")
                                .font(.subheadline)
                            Image(systemName: "chevron.right")
                                .font(.caption2)
                        }
                        .foregroundColor(Color(UIColor.secondaryLabel))
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(UIColor.systemBackground))
                        .cornerRadius(8)
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(UIColor.separator), lineWidth: 1))
                    }
                }
                if !actionsConfig.bulk.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 10) {
                            ForEach(actionsConfig.bulk) { action in
                                BulkActionButton(action: action, onDelete: handleBulkDelete)
                            }
                        }
                        .padding(.horizontal, 2)
                    }
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(UIColor.systemBackground))
    }

    private var selectionFooterView: some View {
        HStack(spacing: 12) {
            Button("Deselect All \(selectedRows.count)") { selectedRows.removeAll() }
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundColor(.primary)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(Color(UIColor.tertiarySystemFill))
                .cornerRadius(8)
                .buttonStyle(.plain)
            Spacer()
            Button {
                // Apply / view selected
            } label: {
                HStack(spacing: 6) {
                    Text("\(selectedRows.count) Selected")
                        .fontWeight(.semibold)
                    Image(systemName: "checkmark")
                        .font(.caption)
                }
                .foregroundColor(.white)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color.accentColor)
                .cornerRadius(8)
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color(UIColor.systemBackground))
        .shadow(color: Color.black.opacity(0.1), radius: 6, x: 0, y: -2)
    }

    private var listContentView: some View {
        ScrollView {
            LazyVStack(spacing: 6) {
                if !selectedRows.isEmpty && selectionEnabled {
                    HStack(spacing: 12) {
                        Button {
                            if selectedRows.count == listData.count {
                                selectedRows.removeAll()
                            } else {
                                selectedRows = Set(listData.map { recordId(from: $0) })
                            }
                        } label: {
                            Image(systemName: selectedRows.count == listData.count ? "checkmark.circle.fill" : "circle")
                                .font(.title2)
                                .foregroundColor(selectedRows.count == listData.count ? .accentColor : Color(UIColor.tertiaryLabel))
                        }
                        .buttonStyle(.plain)
                        Text("Select All")
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundColor(.primary)
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 14)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(10)
                }
                ForEach(Array(listData.enumerated()), id: \.offset) { _, row in
                    tableRowView(row)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .padding(.bottom, (!selectedRows.isEmpty && selectionEnabled) ? 72 : 0)
        }
        .background(Color(UIColor.systemGroupedBackground))
    }

    private func tableRowView(_ row: [String: Any]) -> some View {
        let rid = recordId(from: row)
        let isSelected = selectionEnabled && selectedRows.contains(rid)
        return HStack(alignment: .top, spacing: 8) {
            if selectionEnabled {
                Button {
                    if selectedRows.contains(rid) {
                        selectedRows.remove(rid)
                    } else {
                        selectedRows.insert(rid)
                    }
                } label: {
                    Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                        .font(.title3)
                        .foregroundColor(isSelected ? .accentColor : Color(UIColor.tertiaryLabel))
                }
                .buttonStyle(.plain)
            }
            VStack(alignment: .leading, spacing: 3) {
                HStack(alignment: .center) {
                    Text(primaryText(for: row))
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.primary)
                    Spacer(minLength: 6)
                    if let badge = statusBadge(for: row), !badge.isEmpty {
                        Text(badge)
                            .font(.caption2)
                            .fontWeight(.medium)
                            .foregroundColor(Color(red: 0.2, green: 0.6, blue: 0.3))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(red: 0.85, green: 0.95, blue: 0.85))
                            .cornerRadius(4)
                    }
                }
                ForEach(secondaryLines(for: row), id: \.self) { line in
                    Text(line)
                        .font(.caption)
                        .foregroundColor(Color(UIColor.secondaryLabel))
                        .lineLimit(1)
                }
                HStack {
                    Spacer(minLength: 0)
                    Image(systemName: "chevron.right")
                        .font(.caption2)
                        .foregroundColor(Color(UIColor.tertiaryLabel))
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
            .onTapGesture { handleRowTap(row) }
            if !actionsConfig.row.isEmpty {
                HStack(spacing: 2) {
                    ForEach(actionsConfig.row) { action in
                        Button {
                            handleRowAction(action, row: row)
                        } label: {
                            Image(systemName: iconName(for: action.action))
                                .font(.caption)
                                .foregroundColor(action.action == "delete" ? .red : .primary)
                        }
                    }
                }
            }
        }
        .padding(12)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(isSelected ? Color.accentColor.opacity(0.4) : Color(UIColor.separator.withAlphaComponent(0.3)), lineWidth: isSelected ? 2 : 1)
        )
        .shadow(color: Color.black.opacity(0.06), radius: 2, x: 0, y: 1)
    }

    private func iconName(for action: String) -> String {
        switch action {
        case "eye": return "eye"
        case "edit": return "pencil"
        case "trash", "delete": return "trash"
        case "download": return "arrow.down"
        default: return "circle"
        }
    }

    private var paginationView: some View {
        HStack(spacing: 0) {
            Button {
                if page > 1 { page -= 1; Task { await refetch() } }
            } label: {
                Image(systemName: "chevron.left")
                    .font(.caption)
                    .foregroundColor(page <= 1 ? Color(UIColor.tertiaryLabel) : .primary)
                    .frame(width: 32, height: 28)
            }
            .disabled(page <= 1)
            .buttonStyle(.plain)

            HStack(spacing: 4) {
                ForEach(Array(visiblePaginationSlots().enumerated()), id: \.offset) { _, slot in
                    if let pageNum = slot.pageNum {
                        Button {
                            if pageNum != page { page = pageNum; Task { await refetch() } }
                        } label: {
                            Text("\(pageNum)")
                                .font(.caption)
                                .fontWeight(page == pageNum ? .semibold : .regular)
                                .foregroundColor(page == pageNum ? .white : .primary)
                                .frame(minWidth: 26, minHeight: 26)
                                .background(page == pageNum ? Color.accentColor : Color.clear)
                                .clipShape(Circle())
                                .overlay(
                                    Circle()
                                        .stroke(page == pageNum ? Color.accentColor.opacity(0.5) : Color.clear, lineWidth: 1)
                                )
                        }
                        .buttonStyle(.plain)
                        .disabled(page == pageNum)
                    } else {
                        Text("...")
                            .font(.caption)
                            .foregroundColor(Color(UIColor.tertiaryLabel))
                            .frame(minWidth: 20, minHeight: 26)
                    }
                }
            }
            .frame(maxWidth: .infinity)

            Button {
                if page < totalPages { page += 1; Task { await refetch() } }
            } label: {
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(page >= totalPages ? Color(UIColor.tertiaryLabel) : .primary)
                    .frame(width: 32, height: 28)
            }
            .disabled(totalPages == 0 || page >= totalPages)
            .buttonStyle(.plain)

            Button {
                showPageSizePicker.toggle()
            } label: {
                HStack(spacing: 4) {
                    Text("\(pageSize) / page")
                        .font(.caption)
                        .foregroundColor(.primary)
                    Image(systemName: "chevron.down")
                        .font(.caption2)
                        .foregroundColor(.primary)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 5)
                .background(Color(UIColor.systemBackground))
                .cornerRadius(6)
                .overlay(
                    RoundedRectangle(cornerRadius: 6)
                        .stroke(Color(UIColor.separator.withAlphaComponent(0.35)), lineWidth: 1)
                )
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 6)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(UIColor.separator.withAlphaComponent(0.25)), lineWidth: 1)
        )
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }

    private var pageSizeDropdownView: some View {
        ScrollView {
            VStack(spacing: 0) {
                ForEach([5, 10, 15, 20, 25, 50], id: \.self) { size in
                    Button {
                        if pageSize != size { pageSize = size; page = 1; Task { await refetch() } }
                        showPageSizePicker = false
                    } label: {
                        Text("\(size) / page")
                            .font(.caption2)
                            .foregroundColor(.primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .frame(width: 80)
        .frame(maxHeight: 88)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(6)
        .overlay(
            RoundedRectangle(cornerRadius: 6)
                .stroke(Color(UIColor.separator.withAlphaComponent(0.35)), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.12), radius: 8, x: 0, y: 2)
    }

    // MARK: - Filter drawer (dynamic from filtersConfig)

    private var filterDrawerSheet: some View {
        NavigationView {
            List {
                ForEach(filtersConfig) { config in
                    filterControlRow(for: config)
                }
                if activeFiltersCount > 0 {
                    Button {
                        filters = [:]
                        page = 1
                        Task { await refetch() }
                    } label: {
                        HStack {
                            Image(systemName: "xmark.circle")
                            Text("Clear All")
                        }
                        .foregroundColor(.red)
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Filter \(context.widget.title ?? "Data")")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { isFilterOpen = false }
                }
            }
        }
    }

    // MARK: - Columns visibility (like web Columns dropdown)

    private var columnsVisibilitySheet: some View {
        NavigationView {
            List {
                ForEach(allColumns.filter { $0.key != "actions" }) { col in
                    Toggle(isOn: Binding(
                        get: { visibleColumns.contains { $0.key == col.key } },
                        set: { on in
                            if on {
                                if !visibleColumns.contains(where: { $0.key == col.key }) {
                                    visibleColumns.append(col)
                                }
                            } else {
                                if visibleColumns.count > 1 {
                                    visibleColumns.removeAll { $0.key == col.key }
                                }
                            }
                        }
                    )) {
                        Text(col.label)
                            .font(.subheadline)
                    }
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle("Columns")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { showColumnsSheet = false }
                }
            }
        }
    }

    @ViewBuilder
    private func filterControlRow(for config: FilterFieldConfig) -> some View {
        let label = config.key.reduce("") { res, c in
            (c.isLetter && c.isUppercase && !res.isEmpty) ? res + " " + String(c) : res + String(c)
        }.capitalized
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundColor(.primary)
            switch config.type {
            case "dropdown", "select":
                let sel = Binding<String>(
                    get: { filters[config.key] as? String ?? "All" },
                    set: { newVal in
                        if newVal == "All" { filters.removeValue(forKey: config.key) } else { filters[config.key] = newVal }
                        page = 1
                        Task { await refetch() }
                    }
                )
                Picker("", selection: sel) {
                    Text("All").tag("All")
                    ForEach(config.options, id: \.self) { opt in
                        Text(opt).tag(opt)
                    }
                }
                .pickerStyle(.menu)
            case "text":
                let binding = Binding<String>(
                    get: { filters[config.key] as? String ?? "" },
                    set: { newVal in
                        filters[config.key] = newVal
                        page = 1
                        Task { await refetch() }
                    }
                )
                TextField(config.placeholder ?? "Enter \(config.key)", text: binding)
                    .textFieldStyle(.roundedBorder)
                    .autocapitalization(.none)
            case "date":
                let binding = Binding<String>(
                    get: { filters[config.key] as? String ?? "" },
                    set: { newVal in
                        filters[config.key] = newVal
                        page = 1
                        Task { await refetch() }
                    }
                )
                TextField(config.placeholder ?? "Date (YYYY-MM-DD)", text: binding)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numbersAndPunctuation)
            default:
                EmptyView()
            }
        }
        .padding(.vertical, 4)
    }

    /// Returns array of optional page numbers: nil = ellipsis slot. Handles all cases (0 pages, 1 page, few, many with ellipsis).
    private func visiblePaginationSlots() -> [PaginationSlot] {
        let total = max(0, totalPages)
        if total == 0 { return [] }
        if total <= 5 {
            return (1...total).map { PaginationSlot.pageNum($0) }
        }
        if total <= 7 {
            var slots: [PaginationSlot] = []
            for i in 1...total { slots.append(.pageNum(i)) }
            return slots
        }
        if page <= 4 {
            return [1, 2, 3, 4, 5].map { PaginationSlot.pageNum($0) } + [.ellipsis, .pageNum(total)]
        }
        if page >= total - 3 {
            return [.pageNum(1), .ellipsis] + (total - 4...total).map { PaginationSlot.pageNum($0) }
        }
        return [.pageNum(1), .ellipsis, .pageNum(page - 1), .pageNum(page), .pageNum(page + 1), .ellipsis, .pageNum(total)]
    }

    private var formSheet: some View {
        NavigationStack {
            ComprehensiveTableFormSheet(
                fields: visibleColumns,
                initialData: editingItem ?? [:],
                isLoading: isFormLoading,
                onSubmit: { data in Task { await handleFormSubmit(data) } },
                onDismiss: { isFormOpen = false }
            )
            .navigationTitle(editingItem == nil ? "Add" : "Edit")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { isFormOpen = false }
                }
            }
        }
    }
}

// MARK: - Bulk action button (extracted so ForEach has a single concrete view type)

private struct BulkActionButton: View {
    let action: TableActionConfig
    let onDelete: () -> Void

    private var buttonBackground: some View {
        Color(UIColor.systemBackground)
            .cornerRadius(8)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(UIColor.separator), lineWidth: 1))
    }

    var body: some View {
        if action.action == "delete" {
            AnyView(
                Button(action: onDelete) {
                    Image(systemName: "trash")
                        .font(.body)
                        .foregroundColor(.red)
                        .frame(width: 44, height: 36)
                        .background(buttonBackground)
                }
                .buttonStyle(.plain)
            )
        } else {
            AnyView(
                Button(action: {}) {
                    HStack(spacing: 6) {
                        Image(systemName: action.action.lowercased().contains("owner") ? "person.2" : "arrow.down.circle")
                            .font(.subheadline)
                        Text(action.label)
                            .font(.subheadline)
                    }
                    .foregroundColor(.accentColor)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(buttonBackground)
                }
                .buttonStyle(.plain)
            )
        }
    }
}

// MARK: - Simple form for add/edit

private struct ComprehensiveTableFormSheet: View {
    let fields: [TableColumnConfig]
    let initialData: [String: Any]
    let isLoading: Bool
    let onSubmit: ([String: Any]) -> Void
    let onDismiss: () -> Void

    @State private var formValues: [String: String] = [:]

    private func binding(for key: String) -> Binding<String> {
        Binding(
            get: { formValues[key] ?? "" },
            set: { formValues = formValues.merging([key: $0]) { _, n in n } }
        )
    }

    var body: some View {
        Form {
            ForEach(fields) { field in
                Section(field.label) {
                    TextField(field.label, text: binding(for: field.key))
                }
            }
        }
        .onAppear {
            formValues = fields.reduce(into: [:]) { acc, f in
                let v = initialData[f.key]
                acc[f.key] = v.map { "\($0)" } ?? ""
            }
        }
        .toolbar {
            ToolbarItem(placement: .confirmationAction) {
                Button("Save") {
                    onSubmit(formValues as [String: Any])
                }
                .disabled(isLoading)
            }
        }
    }
}

// MARK: - Fixed bottom bar at screen bottom (above tab bar)
private struct FixedBottomBarHost: View {
    let isActive: Bool
    let barContent: () -> AnyView

    var body: some View {
        FixedBottomBarHostRepresentable(isActive: isActive, barContent: barContent)
            .frame(width: 0, height: 0)
            .allowsHitTesting(false)
    }
}

private struct FixedBottomBarHostRepresentable: UIViewControllerRepresentable {
    let isActive: Bool
    let barContent: () -> AnyView

    func makeUIViewController(context: Context) -> FixedBottomBarHostController {
        FixedBottomBarHostController()
    }

    func updateUIViewController(_ uiViewController: FixedBottomBarHostController, context: Context) {
        uiViewController.update(isActive: isActive, barContent: barContent())
    }
}

/// Full-screen overlay that only receives touches on its subviews (the bar); passes through elsewhere so list can scroll.
private final class PassThroughOverlayView: UIView {
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let hit = super.hitTest(point, with: event)
        return hit == self ? nil : hit
    }
}

private final class FixedBottomBarHostController: UIViewController {
    private var overlayView: UIView?
    private var hostingController: UIHostingController<AnyView>?

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .clear
        view.isUserInteractionEnabled = false
    }

    func update(isActive: Bool, barContent: AnyView) {
        let window: UIWindow? = view.window ?? UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first(where: { $0.isKeyWindow })
        guard let window = window else { return }

        if isActive {
            if overlayView == nil {
                let overlay = PassThroughOverlayView()
                overlay.backgroundColor = .clear
                overlay.translatesAutoresizingMaskIntoConstraints = false
                window.addSubview(overlay)
                NSLayoutConstraint.activate([
                    overlay.topAnchor.constraint(equalTo: window.topAnchor),
                    overlay.leadingAnchor.constraint(equalTo: window.leadingAnchor),
                    overlay.trailingAnchor.constraint(equalTo: window.trailingAnchor),
                    overlay.bottomAnchor.constraint(equalTo: window.bottomAnchor)
                ])
                overlayView = overlay

                let hosting = UIHostingController(rootView: barContent)
                hosting.view.backgroundColor = .clear
                hosting.view.translatesAutoresizingMaskIntoConstraints = false
                overlay.addSubview(hosting.view)
                hostingController = hosting

                NSLayoutConstraint.activate([
                    hosting.view.leadingAnchor.constraint(equalTo: overlay.leadingAnchor),
                    hosting.view.trailingAnchor.constraint(equalTo: overlay.trailingAnchor),
                    hosting.view.heightAnchor.constraint(greaterThanOrEqualToConstant: 56),
                    hosting.view.bottomAnchor.constraint(equalTo: overlay.safeAreaLayoutGuide.bottomAnchor)
                ])
            }
            hostingController?.rootView = barContent
        } else {
            hostingController?.view.removeFromSuperview()
            hostingController = nil
            overlayView?.removeFromSuperview()
            overlayView = nil
        }
    }
}
