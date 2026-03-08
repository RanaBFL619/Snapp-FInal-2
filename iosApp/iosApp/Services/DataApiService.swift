import Foundation
import shared

/// Data API: insert, update, delete. Same endpoints and body shape as web (Api.post/patch/delete).
private let dataBaseURL = "https://snapp-blue-river.fly.dev"

enum DataApiError: Error, LocalizedError {
    case notAuthenticated
    case invalidResponse
    case httpStatus(Int)
    var errorDescription: String? {
        switch self {
        case .notAuthenticated: return "Not authenticated."
        case .invalidResponse: return "Invalid response."
        case .httpStatus(let code): return "Server error (HTTP \(code))."
        }
    }
}

func dataInsert(authToken: String?, schemaType: String, records: [[String: Any]]) async throws {
    guard let token = authToken, !token.isEmpty else { throw DataApiError.notAuthenticated }
    let body: [String: Any] = ["schemaType": schemaType, "records": records]
    let url = URL(string: "\(dataBaseURL)/data/insert")!
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.setValue("application/json", forHTTPHeaderField: "Accept")
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
    request.httpBody = try JSONSerialization.data(withJSONObject: body)

    let (_, response) = try await URLSession.shared.data(for: request)
    guard let http = response as? HTTPURLResponse else { throw DataApiError.invalidResponse }
    if http.statusCode == 401 { SnappKoin.shared.clearSessionDueToUnauthorized(); throw DataApiError.httpStatus(401) }
    guard (200...299).contains(http.statusCode) else { throw DataApiError.httpStatus(http.statusCode) }
}

func dataUpdate(authToken: String?, schemaType: String, records: [[String: Any]]) async throws {
    guard let token = authToken, !token.isEmpty else { throw DataApiError.notAuthenticated }
    let body: [String: Any] = ["schemaType": schemaType, "records": records]
    let url = URL(string: "\(dataBaseURL)/data/update")!
    var request = URLRequest(url: url)
    request.httpMethod = "PATCH"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.setValue("application/json", forHTTPHeaderField: "Accept")
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
    request.httpBody = try JSONSerialization.data(withJSONObject: body)

    let (_, response) = try await URLSession.shared.data(for: request)
    guard let http = response as? HTTPURLResponse else { throw DataApiError.invalidResponse }
    if http.statusCode == 401 { SnappKoin.shared.clearSessionDueToUnauthorized(); throw DataApiError.httpStatus(401) }
    guard (200...299).contains(http.statusCode) else { throw DataApiError.httpStatus(http.statusCode) }
}

/// GET /record/:id — fetch one record for Cancel restore (same as web apiClient.get(`/record/${id}`)).
func dataFetchRecord(authToken: String?, recordId: String) async throws -> [String: Any] {
    guard let token = authToken, !token.isEmpty else { throw DataApiError.notAuthenticated }
    let url = URL(string: "\(dataBaseURL)/record/\(recordId)")!
    var request = URLRequest(url: url)
    request.httpMethod = "GET"
    request.setValue("application/json", forHTTPHeaderField: "Accept")
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

    let (data, response) = try await URLSession.shared.data(for: request)
    guard let http = response as? HTTPURLResponse else { throw DataApiError.invalidResponse }
    if http.statusCode == 401 { SnappKoin.shared.clearSessionDueToUnauthorized(); throw DataApiError.httpStatus(401) }
    guard (200...299).contains(http.statusCode) else { throw DataApiError.httpStatus(http.statusCode) }
    guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
          let dataObj = json["data"] as? [String: Any],
          let records = dataObj["records"] as? [[String: Any]],
          let first = records.first else { return [:] }
    return first
}

func dataDelete(authToken: String?, schemaType: String, recordIds: [String]) async throws {
    guard let token = authToken, !token.isEmpty else { throw DataApiError.notAuthenticated }
    let body: [String: Any] = ["schemaType": schemaType, "recordIds": recordIds]
    let url = URL(string: "\(dataBaseURL)/data/delete")!
    var request = URLRequest(url: url)
    request.httpMethod = "DELETE"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
    request.httpBody = try JSONSerialization.data(withJSONObject: body)

    let (_, response) = try await URLSession.shared.data(for: request)
    guard let http = response as? HTTPURLResponse else { throw DataApiError.invalidResponse }
    if http.statusCode == 401 { SnappKoin.shared.clearSessionDueToUnauthorized(); throw DataApiError.httpStatus(401) }
    guard (200...299).contains(http.statusCode) else { throw DataApiError.httpStatus(http.statusCode) }
}
