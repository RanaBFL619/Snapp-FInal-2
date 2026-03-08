import Foundation

/// Sealed: LoadPage | OpenRecord | CRUD. See ARCHITECTURE Widget/.
enum WidgetAction {
    case loadPage(dataKey: String, page: Int)
    case openRecord(id: String)
}
