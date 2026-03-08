package com.snapp.data.api

import com.snapp.data.api.request.TableRequest
import com.snapp.data.model.auth.LoginRequest
import com.snapp.data.model.auth.LoginResponse
import com.snapp.data.model.layout.LayoutConfig
import com.snapp.data.model.page.PageConfig
import com.snapp.data.model.record.DataDeleteRequest
import com.snapp.data.model.record.DataInsertRequest
import com.snapp.data.model.record.DataUpdateRequest
import com.snapp.data.model.widget.TableResponse
import com.snapp.network.sharedJson
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class SnappApiClient(
    private val client: HttpClient,
    private val baseUrl: String,
    private val tokenStorage: com.snapp.data.store.TokenStorage
) {
    private var authToken: String? = null

    fun setAuthToken(token: String?) { authToken = token }
    fun getAuthToken(): String? = authToken

    private fun url(path: String) = baseUrl.trimEnd('/') + path

    /** Use token from storage before each request (same as web getHeaders()). */
    private fun ensureTokenFromStorage() {
        tokenStorage.getToken()?.let { setAuthToken(it) }
    }

    private fun authHeader(): Pair<String, String>? =
        authToken?.let { "Authorization" to "Bearer $it" }

    suspend fun login(request: LoginRequest): LoginResponse {
        val text = client.post(url(ApiEndpoints.AUTH_LOGIN)) {
            contentType(ContentType.Application.Json)
            authHeader()?.let { (k, v) -> header(k, v) }
            setBody(sharedJson.encodeToString(LoginRequest.serializer(), request))
        }.bodyAsText()
        return sharedJson.decodeFromString(LoginResponse.serializer(), text)
    }

    suspend fun getLayout(): LayoutConfig {
        ensureTokenFromStorage()
        val text = client.get(url(ApiEndpoints.META_LAYOUT)) {
            authHeader()?.let { (k, v) -> header(k, v) }
        }.bodyAsText()
        return sharedJson.decodeFromString(LayoutConfig.serializer(), text)
    }

    suspend fun getPageConfig(slug: String): PageConfig {
        ensureTokenFromStorage()
        val text = client.get(url("${ApiEndpoints.META_PAGE}/$slug")) {
            authHeader()?.let { (k, v) -> header(k, v) }
        }.bodyAsText()
        return sharedJson.decodeFromString(PageConfig.serializer(), text)
    }

    suspend fun getWidgetData(dataKey: String, body: JsonObject? = null): JsonObject {
        ensureTokenFromStorage()
        val payload = body ?: buildJsonObject { }
        val text = client.post(url("${ApiEndpoints.DATA_VIEW}/$dataKey")) {
            contentType(ContentType.Application.Json)
            authHeader()?.let { (k, v) -> header(k, v) }
            setBody(sharedJson.encodeToString(JsonObject.serializer(), payload))
        }.bodyAsText()
        return sharedJson.decodeFromString(JsonObject.serializer(), text)
    }

    suspend fun getTableWidgetData(dataKey: String, request: TableRequest): TableResponse {
        ensureTokenFromStorage()
        val text = client.post(url("${ApiEndpoints.DATA_VIEW}/$dataKey")) {
            contentType(ContentType.Application.Json)
            authHeader()?.let { (k, v) -> header(k, v) }
            setBody(sharedJson.encodeToString(TableRequest.serializer(), request))
        }.bodyAsText()
        return sharedJson.decodeFromString(TableResponse.serializer(), text)
    }

    suspend fun logout() {
        ensureTokenFromStorage()
        client.post(url(ApiEndpoints.AUTH_LOGOUT)) {
            authHeader()?.let { (k, v) -> header(k, v) }
        }
    }

    suspend fun getRecord(id: String): JsonObject {
        ensureTokenFromStorage()
        val text = client.get(url("${ApiEndpoints.RECORD_GET}/$id")) {
            authHeader()?.let { (k, v) -> header(k, v) }
        }.bodyAsText()
        return sharedJson.decodeFromString(JsonObject.serializer(), text)
    }

    suspend fun insertData(request: DataInsertRequest): JsonObject {
        ensureTokenFromStorage()
        val text = client.post(url(ApiEndpoints.DATA_INSERT)) {
            contentType(ContentType.Application.Json)
            authHeader()?.let { (k, v) -> header(k, v) }
            setBody(sharedJson.encodeToString(DataInsertRequest.serializer(), request))
        }.bodyAsText()
        return sharedJson.decodeFromString(JsonObject.serializer(), text)
    }

    suspend fun updateData(request: DataUpdateRequest): JsonObject {
        ensureTokenFromStorage()
        val text = client.patch(url(ApiEndpoints.DATA_UPDATE)) {
            contentType(ContentType.Application.Json)
            authHeader()?.let { (k, v) -> header(k, v) }
            setBody(sharedJson.encodeToString(DataUpdateRequest.serializer(), request))
        }.bodyAsText()
        return sharedJson.decodeFromString(JsonObject.serializer(), text)
    }

    suspend fun deleteData(request: DataDeleteRequest) {
        ensureTokenFromStorage()
        client.delete(url(ApiEndpoints.DATA_DELETE)) {
            contentType(ContentType.Application.Json)
            authHeader()?.let { (k, v) -> header(k, v) }
            setBody(sharedJson.encodeToString(DataDeleteRequest.serializer(), request))
        }
    }
}
