import SwiftUI
import shared

/// Card container — iOS counterpart of client CardWidget.tsx.
/// Renders widget.components in a grid (ContainerLayoutView replaces ResponsiveGridLayout), passes full context to children.
/// Edit/Save/Cancel/Delete when options.readOnly / canDelete and record context; create mode when detailsData == nil.
/// Web: showSaveForNew = fieldCount > 0 && typeof options.readOnly === "boolean"; Cancel fetches /record/:id and restores fields.
struct CardWidgetView: View {
    let context: WidgetRenderContext

    @State private var isFieldEditable = false
    @State private var isEditing = false
    @State private var isCanceling = false
    @State private var isDeleting = false
    @State private var isDeleteFormOpen = false
    @State private var errorMessage: String?

    private var readOnly: Bool? { context.widget.getOptionsReadOnly()?.boolValue }
    private var canDelete: Bool? { context.widget.getOptionsCanDelete()?.boolValue }
    private var noShadow: Bool? { context.widget.getOptionsNoShadow()?.boolValue }
    private var recordId: String? { context.detailsData?["recordId"] as? String }
    private var schemaType: String { context.widget.schema ?? context.widget.dataKey ?? context.widget.id }
    private var hasFieldComponents: Bool {
        (context.widget.components ?? []).contains { $0.type == "field" }
    }
    /// Web: showSaveForNew = fieldCount > 0 && typeof options.readOnly === "boolean" (no detailsFormData check).
    private var showSaveForNew: Bool {
        hasFieldComponents && readOnly == false
    }
    private var showEditButton: Bool { readOnly == false }
    private var showDeleteButton: Bool { (canDelete ?? false) && recordId != nil }
    /// Create mode: no existing record, has fields, form card — start editable (web useEffect sets isFieldEditable true).
    private var isCreateMode: Bool {
        context.detailsData == nil && hasFieldComponents && readOnly == false
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            if let title = context.widget.title, !title.isEmpty || showButtonBar {
                HStack {
                    if let t = context.widget.title, !t.isEmpty {
                        Text(t)
                            .font(.headline)
                            .padding(.horizontal)
                            .padding(.top)
                    }
                    Spacer()
                    if showButtonBar {
                        buttonBar
                    }
                }
            }

            if let components = context.widget.components, !components.isEmpty {
                let columns: Int = context.widget.getLayoutColumns().map { Int($0.int32Value) } ?? 12
                let layout = LayoutHelper.generateMobileLayout(
                    widgets: Array(components),
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
                .padding(.horizontal)
                .padding(.bottom)
            }

            if let msg = errorMessage {
                Text(msg)
                    .font(.caption)
                    .foregroundColor(.red)
                    .padding(.horizontal)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(UIColor.systemBackground))
        .cornerRadius(12)
        .shadow(
            color: (noShadow == true) ? .clear : Color.black.opacity(0.08),
            radius: (noShadow == true) ? 0 : 4,
            x: 0,
            y: 2
        )
        .confirmationDialog("Delete?", isPresented: $isDeleteFormOpen, titleVisibility: .visible) {
            Button("Delete", role: .destructive) { Task { await handleDeleteConfirm() } }
            Button("Cancel", role: .cancel) { isDeleteFormOpen = false }
        } message: {
            Text("This action cannot be undone.")
        }
        .onAppear {
            if isCreateMode { isFieldEditable = true }
        }
    }

    private var showButtonBar: Bool {
        if context.detailsData != nil {
            if isFieldEditable { return true }
            return showEditButton || showDeleteButton
        }
        return showSaveForNew
    }

    @ViewBuilder
    private var buttonBar: some View {
        HStack(spacing: 8) {
            if context.detailsData != nil {
                if isFieldEditable {
                    Button(isCanceling ? "Canceling..." : "Cancel") { Task { await handleCancelClick() } }
                        .disabled(isCanceling || isEditing)
                    Button(isEditing ? "Saving..." : "Save") { Task { await handleFormSubmit() } }
                        .disabled(isEditing || isCanceling)
                } else {
                    if showEditButton {
                        Button("Edit") { isFieldEditable = true }
                            .disabled(isEditing)
                    }
                    if showDeleteButton {
                        Button("Delete", role: .destructive) { isDeleteFormOpen = true }
                            .disabled(isDeleting)
                    }
                }
            } else if showSaveForNew {
                Button(isEditing ? "Saving..." : "Save") { Task { await handleFormSubmit() } }
                    .disabled(isEditing)
            }
        }
        .buttonStyle(.bordered)
        .padding(.trailing, 8)
    }

    /// Web: fetch /record/:id and restore each field via handleInputChange; else just exit edit mode.
    private func handleCancelClick() async {
        isCanceling = true
        errorMessage = nil
        if let id = recordId {
            let token = SnappKoin.shared.getStoredSession()?.token
            do {
                let recordData = try await dataFetchRecord(authToken: token, recordId: id)
                let fieldComponents = (context.widget.components ?? []).filter { $0.type == "field" && $0.dataKey != nil }
                for field in fieldComponents {
                    guard let dataKey = field.dataKey, recordData[dataKey] != nil else { continue }
                    context.onInputChange(field.id, dataKey, recordData[dataKey]!)
                }
            } catch {
                errorMessage = error.localizedDescription
            }
        } else {
            context.onResetDetailsFormData()
        }
        isFieldEditable = false
        isCanceling = false
    }

    private func handleFormSubmit() async {
        isEditing = true
        errorMessage = nil
        let token = SnappKoin.shared.getStoredSession()?.token

        let requiredKeys: [String] = Array(context.widget.getRequiredFieldDataKeys())
        func validateRequired(_ data: [String: Any]) -> Bool {
            requiredKeys.contains { key in
                let v = data[key]
                if v == nil { return true }
                if let s = v as? String, s.isEmpty { return true }
                return false
            }
        }

        if context.detailsData == nil && hasFieldComponents && readOnly == false {
            let data = context.detailsFormData
            if validateRequired(data) {
                errorMessage = "Please fill in all required fields."
                isEditing = false
                return
            }
            let records = [data]
            let isEmpty = records.allSatisfy { (obj: [String: Any]) in obj.isEmpty }
            if isEmpty {
                errorMessage = "Record is empty, nothing to save."
                isEditing = false
                return
            }
            do {
                try await dataInsert(authToken: token, schemaType: schemaType, records: records)
                context.onResetDetailsFormData()
                isEditing = false
                return
            } catch {
                errorMessage = error.localizedDescription
            }
            isEditing = false
            return
        }

        guard let data = context.detailsData else {
            isEditing = false
            return
        }
        if validateRequired(data) {
            errorMessage = "Please fill all required fields."
            isEditing = false
            return
        }
        var record = data
        if let rid = recordId { record["recordId"] = rid }
        do {
            try await dataUpdate(authToken: token, schemaType: schemaType, records: [record])
            isFieldEditable = false
        } catch {
            errorMessage = error.localizedDescription
        }
        isEditing = false
    }

    private func handleDeleteConfirm() async {
        guard let id = recordId else { isDeleteFormOpen = false; isDeleting = false; return }
        isDeleting = true
        errorMessage = nil
        let token = SnappKoin.shared.getStoredSession()?.token
        let schema = schemaType
        do {
            try await dataDelete(authToken: token, schemaType: schema, recordIds: [id])
            context.onResetDetailsFormData()
            context.onAction(.openRecord(id: ""))
        } catch {
            errorMessage = error.localizedDescription
        }
        isDeleting = false
        isDeleteFormOpen = false
    }
}
